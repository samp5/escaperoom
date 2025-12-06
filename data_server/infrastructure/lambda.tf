locals {
  build_location = "../build/server.zip"
  function_name  = "DataServer"
}

data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "role" {
  name               = "data_server_execution_role"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

resource "aws_iam_policy" "access_policy" {
  name = "access_policy"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Action : [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Effect : "Allow",
        Resource : "arn:aws:logs:*:*:*"
      },
      {
        "Action" : [
          "dynamodb:GetItem",
          "dynamodb:BatchWriteItem",
          "dynamodb:DeleteItem",
          "dynamodb:PutItem"
        ],
        "Effect" : "Allow",
        "Resource" : aws_dynamodb_table.user_table.arn
      },
      {
        "Action" : [
          "s3:DeleteObject",
          "s3:GetObject",
          "s3:GetObjectTagging",
          "s3:ListBucket",
          "s3:PutObject",
          "s3:PutObjectTagging"
        ],
        "Effect" : "Allow",
        "Resource" : [
          aws_s3_bucket.map_files.arn,
          format("%s/*", aws_s3_bucket.map_files.arn)
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "logging" {
  role       = aws_iam_role.role.id
  policy_arn = aws_iam_policy.access_policy.arn
}

resource "aws_lambda_permission" "apigw" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.data_server.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_rest_api.gateway.execution_arn}/*/*"
}

resource "aws_cloudwatch_log_group" "lambda_log_group" {
  name              = "/aws/lambda/${local.function_name}"
  retention_in_days = 7
  lifecycle {
    prevent_destroy = false
  }
}

resource "aws_lambda_function" "data_server" {
  filename         = local.build_location
  source_code_hash = filebase64sha256(local.build_location)
  function_name    = local.function_name
  role             = aws_iam_role.role.arn
  handler          = "bootstrap"
  depends_on       = [aws_cloudwatch_log_group.lambda_log_group]

  runtime       = "provided.al2023"
  architectures = ["arm64"]
}
