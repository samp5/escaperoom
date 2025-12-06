package dynamo

import "github.com/google/uuid"

type User struct {
	Name      string    `json:"username"`
	Password  string    `json:"password"`
	ID        uuid.UUID `json:"id"`
	EncKey    uuid.UUID `json:"encode_key"`
	AccessKey uuid.UUID `json:"access_key"`
}
