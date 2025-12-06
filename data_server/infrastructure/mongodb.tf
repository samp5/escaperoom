resource "mongodbatlas_advanced_cluster" "escaperoom" {
  project_id   = "66db30aa72dc5d5bc91f7097"
  name         = "EscapeRoom"
  cluster_type = "REPLICASET"

  replication_specs = [
    {
      region_configs = [
        {
          electable_specs = {
            instance_size = "M0"
          }
          provider_name         = "TENANT"
          backing_provider_name = "AWS"
          region_name           = "US_EAST_1"
          priority              = 7
        }
      ]
    }
  ]
}

resource "mongodbatlas_database_user" "lambda_user" {
  username           = aws_iam_role.role.arn
  description        = "tf generated lambda user"
  project_id         = "66db30aa72dc5d5bc91f7097"
  auth_database_name = "$external"
  aws_iam_type       = "ROLE"

  roles {
    role_name     = "readWriteAnyDatabase"
    database_name = "admin"
  }

  scopes {
    name = "EscapeRoom"
    type = "CLUSTER"
  }
}
