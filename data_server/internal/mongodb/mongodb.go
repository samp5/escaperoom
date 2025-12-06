package mongodb

import (
	"context"
	"data_server/internal/config"
	"data_server/internal/log"
	"fmt"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

var (
	server = options.ServerAPI(options.ServerAPIVersion1)
	url    = "mongodb+srv://escaperoom.yjf0c1w.mongodb.net/?authSource=%24external&authMechanism=MONGODB-AWS"
	opts   = options.Client().ApplyURI(url).SetServerAPIOptions(server)
)

type MongoDB struct {
	client   *mongo.Client
	db_name  string
	meta_col string
	user_rec_col string
}

func NewMongoDB(cfg *config.Config) (*MongoDB, error) {
	mongo, err := mongo.Connect(opts)
	if err != nil {
		log.WithError(err).Error("failed to connect to mongodb")
		return nil, err
	}

	return &MongoDB{
		client:   mongo,
		db_name:  cfg.MongoDB.DbName,
		meta_col: cfg.MongoDB.Collections.MapMetadata,
		user_rec_col: cfg.MongoDB.Collections.PlayerRecords,
	}, nil
}

func (db *MongoDB) putInCollection(ctx context.Context, collection, main_key string, item map[string]any) error {
	col := db.client.Database(db.db_name).Collection(collection)

	filter := bson.M{main_key: item[main_key]}
	opts := options.Replace().SetUpsert(true)
	_, err := col.ReplaceOne(ctx, filter, item, opts)
	if err != nil {
		log.WithError(err).Error("putInCollection: failed to upload json object to mongo")
		return err
	}

	return nil
}

func (db *MongoDB) getFromCollection(ctx context.Context, collection, key, value string) (map[string]any, error) {
	col := db.client.Database(db.db_name).Collection(collection)

	opts := options.FindOne().SetProjection(bson.M{"_id": 0})
	item := col.FindOne(ctx, bson.M{key: value}, opts)
	err := item.Err()
	if err == mongo.ErrNoDocuments {
		return nil, nil
	}
	if err != nil {
		log.WithError(err).WithField(key, value).Error("getFromCollection: failed to get object")
		return nil, err
	}

	var json map[string]any
	err = item.Decode(&json)
	if err != nil {
		log.WithError(err).Error("getFromCollection: unable to decode mongodb item into map")
		return nil, err
	}

	return json, nil
}

func (db *MongoDB) listFromCollection(ctx context.Context, collection string, args *ListArguments, exclude_fields []string) ([]map[string]any, error) {
	col := db.client.Database(db.db_name).Collection(collection)

	filter := make(map[string]bson.M)
	for index := range args.FilterFields {
		field := args.FilterFields[index]
		method := fmt.Sprintf("$%s", args.FilterMethods[index])
		value := args.FilterValues[index]

		if _, ok := filter[field]; ok {
			filter[field][method] = value
		} else {
			filter[field] = bson.M{method: value}
		}
	}

	inclusion := bson.M{}
	if len(filter) != 0 {
		filters := make([]any, 0, len(filter))
		for statname, conditions := range filter {
			filters = append(filters, map[string]bson.M{statname: conditions})
		}
		inclusion = bson.M{fmt.Sprintf("$%s", args.FiltersApproach): filters}
	}

	excludes := bson.M{"_id": 0}
	for _, field := range exclude_fields {
		excludes[field] = 0;
	}

	opts := options.Find().SetLimit(args.Limit).SetProjection(excludes)
	if args.SortField != "" {
		opts = opts.SetSort(bson.M{args.SortField: args.SortDir})
	}

	output, err := col.Find(ctx, inclusion, opts)
	if err != nil {
		log.WithError(err).Error("unable to list entries from mongodb")
		return nil, err
	}

	results := make([]map[string]any, 0, 16)
	err = output.All(ctx, &results)
	if err != nil {
		log.WithError(err).Error("unable to parse results from mongodb")
		return nil, err
	}

	return results, nil
}

func (db *MongoDB) deleteFromCollection(ctx context.Context, collection, key, value string) (bool, error) {
	col := db.client.Database(db.db_name).Collection(collection)

	result, err := col.DeleteOne(ctx, bson.M{key: value})
	if err != nil {
		log.WithError(err).Error("failed to delete entry from mongodb")
		return false, err
	}

	return result.DeletedCount == 1, nil
}

func (db *MongoDB) PutMetadata(ctx context.Context, metadata map[string]any) error {
	return db.putInCollection(ctx, db.meta_col, "id", metadata)
}

func (db *MongoDB) GetMetadata(ctx context.Context, mapID string) (map[string]any, error) {
	return db.getFromCollection(ctx, db.meta_col, "id", mapID)
}

func (db *MongoDB) ListMetadata(ctx context.Context, args *ListArguments) ([]map[string]any, error) {
	return db.listFromCollection(ctx, db.meta_col, args, nil)
}

func (db *MongoDB) DeleteMetadata(ctx context.Context, mapID string) (bool, error) {
	return db.deleteFromCollection(ctx, db.meta_col, "id", mapID)
}


func (db *MongoDB) UpdateMapForClear(ctx context.Context, username, mapID string, clearTime int64) (error, error) {
	collection := db.client.Database(db.db_name).Collection(db.meta_col)

	// first update the clear count
	original := collection.FindOneAndUpdate(ctx, bson.M{"id": mapID}, bson.M{"$inc": bson.M{"stats.clears": 1}})
	if err := original.Err(); err != nil {
		if err == mongo.ErrNoDocuments {
			return err, nil
		} else {
			log.WithError(err).Error("failed to update map clear count")
			return nil, err
		}
	}

	meta := MapMetadata{}
	err := original.Decode(&meta)
	if err != nil {
		log.WithError(err).Error("failed to parse mongo response")
		return nil, err
	}

	// if this clear was faster, update
	if clearTime < meta.Stats.Record.FastestClearMS || meta.Stats.Record.FastestClearMS == -1 {
		original := collection.FindOneAndUpdate(ctx, bson.M{"id": mapID}, bson.M{"$set": bson.M{"stats.record.fastestms": clearTime, "stats.record.username": username}})
		if err := original.Err(); err != nil {
			if err == mongo.ErrNoDocuments {
				return err, nil
			} else {
				log.WithError(err).Error("failed to update map clear count")
				return nil, err
			}
		}
	}

	return nil, nil
}

func (db *MongoDB) UpdateMapStats(ctx context.Context, mapID string, toIncrease map[string]struct{}) (error, error) {
	collection := db.client.Database(db.db_name).Collection(db.meta_col)

	increases := bson.M{}
	for increase := range toIncrease {
		increases[fmt.Sprintf("stats.%s", increase)] = 1
	}
	update := bson.M{"$inc": increases}

	original := collection.FindOneAndUpdate(ctx, bson.M{"id": mapID}, update)
	if err := original.Err(); err != nil {
		if err == mongo.ErrNoDocuments {
			return err, nil
		} else {
			log.WithError(err).Error("failed to update map stats")
			return nil, err
		}
	}

	return nil, nil
}


func (db *MongoDB) PutPlayerRecord(ctx context.Context, record map[string]any) error {
	return db.putInCollection(ctx, db.user_rec_col, "username", record)
}

func (db *MongoDB) GetPlayerRecord(ctx context.Context, username string) (map[string]any, error) {
	return db.getFromCollection(ctx, db.user_rec_col, "username", username)
}

func (db *MongoDB) ListPlayerRecords(ctx context.Context, args *ListArguments) ([]map[string]any, error) {
	return db.listFromCollection(ctx, db.user_rec_col, args, nil)
}

func (db *MongoDB) DeletePlayerRecord(ctx context.Context, username string) (bool, error) {
	return db.deleteFromCollection(ctx, db.user_rec_col, "username", username)
}
