package domain

// Violations stores api_violations response
type Violations struct {
	Violations      []Violation     `json:"violations"`
	ViolationsCount ViolationsCount `json:"violations_count"`
	Message         string          `json:"message"`
}

// Must returns must violations
func (v *Violations) Must() []Violation {
	return v.filterViolations(Must)
}

// Should returns should violations
func (v *Violations) Should() []Violation {
	return v.filterViolations(Should)
}

// May returns may violations
func (v *Violations) May() []Violation {
	return v.filterViolations(May)
}

// Hint returns hint violations
func (v *Violations) Hint() []Violation {
	return v.filterViolations(Hint)
}

func (v *Violations) filterViolations(violationType ViolationType) []Violation {
	result := []Violation{}
	for _, violation := range v.Violations {
		if violation.ViolationType == violationType {
			result = append(result, violation)
		}
	}
	return result
}
