package domain

import (
	"testing"

	"github.com/zalando/zally/cli/zally/tests"
)

func TestViolations(t *testing.T) {
	var mustViolation Violation
	mustViolation.Title = "Must Title"
	mustViolation.RuleLink = "http://example.com/mustViolation"
	mustViolation.ViolationType = Must
	mustViolation.Description = "Must Description"
	mustViolation.Pointer = "/pointer/1"
	mustViolation.Paths = []string{"/path/one", "/path/two"}

	var shouldViolation Violation
	shouldViolation.Title = "Should Title"
	shouldViolation.RuleLink = "http://example.com/shouldViolation"
	shouldViolation.ViolationType = Should
	shouldViolation.Description = "Should Description"
	shouldViolation.Paths = []string{"/path/three", "/path/four"}

	var mayViolation Violation
	mayViolation.Title = "May Title"
	mayViolation.RuleLink = "http://example.com/mayViolation"
	mayViolation.ViolationType = May
	mayViolation.Description = "May Description"
	mayViolation.Paths = []string{"/path/five", "/path/six"}

	var hintViolation Violation
	hintViolation.Title = "Hint Title"
	hintViolation.RuleLink = "http://example.com/hintViolation"
	hintViolation.ViolationType = Hint
	hintViolation.Description = "Hint Description"
	hintViolation.Pointer = "/pointer/2"

	var violationsCount ViolationsCount
	violationsCount.Must = 1
	violationsCount.Should = 2
	violationsCount.May = 3
	violationsCount.Hint = 4

	var violations Violations
	violations.Violations = []Violation{mustViolation, shouldViolation, mayViolation, hintViolation}
	violations.ViolationsCount = violationsCount

	t.Run("filterViolations returns filtered list of MUST violations", func(t *testing.T) {
		actualResult := violations.filterViolations(Must)
		expectedResult := []Violation{mustViolation}

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("filterViolations returns filtered list of SHOULD violations", func(t *testing.T) {
		actualResult := violations.filterViolations(Should)
		expectedResult := []Violation{shouldViolation}

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("filterViolations uses case-insensitive type", func(t *testing.T) {
		actualResult := violations.filterViolations(Should)
		expectedResult := []Violation{shouldViolation}

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("Must filters by MUST violations", func(t *testing.T) {
		actualResult := violations.Must()
		expectedResult := violations.filterViolations(Must)

		tests.AssertEquals(t, expectedResult, actualResult)

	})

	t.Run("Should filters by SHOULD violations", func(t *testing.T) {
		actualResult := violations.Should()
		expectedResult := violations.filterViolations(Should)

		tests.AssertEquals(t, expectedResult, actualResult)

	})

	t.Run("May filters by MAY violations", func(t *testing.T) {
		actualResult := violations.May()
		expectedResult := violations.filterViolations(May)

		tests.AssertEquals(t, expectedResult, actualResult)

	})

	t.Run("Hint filters by HINT violations", func(t *testing.T) {
		actualResult := violations.Hint()
		expectedResult := violations.filterViolations(Hint)

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}
