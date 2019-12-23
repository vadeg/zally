package domain

import (
	"encoding/json"
	"errors"
	"fmt"
	"strings"
)

type ViolationType int

const (
	// Constants defined by rank 'must->should->may->hint'
	Must ViolationType = iota
	Should
	May
	Hint
)

func (vt ViolationType) String() string {
	return [...]string{"must", "should", "may", "hint"}[vt]
}

func (vt *ViolationType) UnmarshalJSON(b []byte) error {
	var s string
	if err := json.Unmarshal(b, &s); err != nil {
		return err
	}
	switch strings.ToLower(s) {
	case "must":
		*vt = Must
	case "should":
		*vt = Should
	case "may":
		*vt = May
	case "hint":
		*vt = Hint
	default:
		return errors.New(fmt.Sprintf("Unsupported violation level type: %s", s))
	}
	return nil
}
