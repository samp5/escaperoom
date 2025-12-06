package encryption

import (
	"crypto/sha256"
	"encoding/hex"

	"github.com/google/uuid"
)

var (
	validSalt = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789?-_!`"
)

type encryptor struct {
	key uuid.UUID
	pos uint8
	dir int8 // -1 or 1
}

func NewEncryptor(key uuid.UUID) encryptor {
	return encryptor{
		key: key,
		pos: 0,
		dir: 1,
	}
}

func (enc *encryptor) EncryptPassword(password string) string {
	start := enc.nextByte()
	enc.moveDir(int8(start))

	frontSalt := enc.createSalt(int(enc.nextByte()) % 12 + 4)
	backSalt := enc.createSalt(int(enc.nextByte()) % 12 + 4)

	salted := frontSalt + password + backSalt
	uaqrypted := enc.uaqrypt(salted)
	encrypted := sha256.Sum256([]byte(uaqrypted))

	return string(hex.EncodeToString(encrypted[:]))
}

func (enc *encryptor) uaqrypt(str string) string {
	uaqrypted := make([]byte, len(str))

	start := enc.nextByte()
	enc.moveDir(int8(start))

	untilDirSwitch := enc.nextByte() % 7

	for i, char := range str {

		if untilDirSwitch == 0 {
			untilDirSwitch = enc.nextByte() % 7

			a := enc.nextByte()
			if a > 127 {
				enc.dir = 1
			} else {
				enc.dir = -1
			}

			enc.moveDir(int8(enc.nextInt() % 51))
		} else {
			untilDirSwitch--
		}

		num := enc.nextInt()
		encChar := char ^ num
		uaqrypted[i] = byte(encChar)

		drink := enc.nextByte() % 17
		enc.moveDir(int8(drink))

	}

	return hex.EncodeToString(uaqrypted)
}

func (enc *encryptor) nextByte() byte {
	var ret byte = 0

	if byteOffset := enc.pos % 8; byteOffset == 0 {
		ret = enc.key[enc.pos/8]
	} else {
		ret = enc.key[enc.pos/8]
		byt1 := enc.key[((enc.pos+8)%128)/8]

		ret = ret << byteOffset
		ret |= byt1 >> (8 - byteOffset)
	}

	enc.moveDir(8)
	return ret
}

func (enc *encryptor) nextInt() int32 {
	var ret int32 = 0

	if byteOffset := enc.pos % 8; byteOffset == 0 {
		baseNdx := enc.pos / 8
		for i := range 4 {
			byt := enc.key[(baseNdx+uint8(i))%16]
			ret |= int32(byt) << (8 * (3 - i))
		}
	} else {
		bytes := make([]byte, 5)

		// get all used bytes
		baseNdx := enc.pos / 8
		for i := range 5 {
			bytes[i] = enc.key[(baseNdx+uint8(i))%16]
		}

		// get bytes with correct offsets
		offsetBytes := make([]byte, 4)
		for i := range 4 {
			byt := bytes[i]
			byt1 := bytes[i+1]

			byt = byt << byteOffset
			byt |= byt1 >> (8 - byteOffset)

			offsetBytes[i] = byt
		}

		// combine to an int
		for i, byt := range offsetBytes {
			ret |= int32(byt) << (8 * (3 - i))
		}
	}

	enc.moveDir(32)
	return ret
}

func (enc *encryptor) moveDir(amt int8) {
	// calc new pos
	newPos := int(enc.pos) + int(amt*enc.dir)

	// handle overflow
	if newPos < 0 {
		newPos = newPos + 128
	} else if newPos >= 128 {
		newPos = newPos - 128
	}

	// set new pos
	enc.pos = uint8(newPos)
}

func (enc *encryptor) createSalt(size int) string {
	out := make([]byte, size)

	for i := range size {
		out[i] = validSalt[int(enc.nextByte())%len(validSalt)]
	}

	return string(out)
}
