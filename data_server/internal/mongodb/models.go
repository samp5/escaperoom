package mongodb

// an incomplete representation. only currently needed fields are included
type MapMetadata struct {
	Stats struct {
		Record struct {
			FastestClearMS int64  `bson:"fastestms"`
			Username       string `bson:"username"`
		} `bson:"record"`
	} `bson:"stats"`
}

type ListArguments struct {
	Limit           int64
	SortField       string
	SortDir         int
	FilterFields    []string
	FilterMethods   []string
	FilterValues    []any
	FiltersApproach string
}
