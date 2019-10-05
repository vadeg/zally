package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Test
import kotlin.reflect.KClass

class RulesPolicyTest {

    /** TestRule used for testing RulesPolicy */
    @Rule(
        ruleSet = TestRuleSet::class,
        id = "TestRule",
        severity = Severity.MUST,
        title = "Test Rule"
    )
    private class MustRule(val result: Violation?) {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun validate(swagger: Swagger): Violation? = result
    }

    @Rule(
        ruleSet = TestRuleSet::class,
        id = "ShouldTestRule",
        severity = Severity.SHOULD,
        title = "Test Rule with 'SHOULD' severity"
    )
    private class ShouldRule(val result: Violation?) {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.SHOULD)
        fun validate(swagger: Swagger): Violation? = result
    }

    @Rule(
        ruleSet = TestRuleSet::class,
        id = "MayTestRule",
        severity = Severity.MAY,
        title = "Test Rule with 'MAY' severity"
    )
    private class MayRule(val result: Violation?) {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MAY)
        fun validate(swagger: Swagger): Violation? = result
    }

    @Rule(
        ruleSet = TestRuleSet::class,
        id = "HintTestRule",
        severity = Severity.HINT,
        title = "Test Rule with 'HINT' severity"
    )
    private class HintRule(val result: Violation?) {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.HINT)
        fun validate(swagger: Swagger): Violation? = result
    }

    @Test
    fun `should accept rule if not filtered`() {
        val policy = RulesPolicy(arrayOf("TestCheckApiNameIsPresentJsonRule", "136"))
        assertTrue(policy.accepts(mustRule()))
    }

    @Test
    fun `should not accept rule if filtered`() {
        val policy = RulesPolicy(arrayOf("TestCheckApiNameIsPresentJsonRule", "TestRule"))
        assertFalse(policy.accepts(mustRule()))
    }

    @Test
    fun `with more ignores allows extension`() {
        val original = RulesPolicy(emptyArray())
        assertTrue(original.accepts(mustRule()))

        val extended = original.withMoreIgnores(listOf("TestCheckApiNameIsPresentJsonRule", "TestRule"))
        assertFalse(extended.accepts(mustRule()))

        // original is unmodified
        assertTrue(original.accepts(mustRule()))
    }

    @Test
    fun `should filter rules with lower severity`() {
        val original = RulesPolicy(emptyArray())
        val allRules = allRules()

        assertThat(allRules().filter { original.accepts(it) }, equalTo(allRules))

        val atLeastMayPolicy = original.withMinSeverity(Severity.MAY)
        val answer = listOf(extractRule(MayRule::class), extractRule(HintRule::class))
        assertThat(allRules().filter { atLeastMayPolicy.accepts(it) }, equalTo(answer))
    }

    private fun mustRule() = extractRule(MustRule::class)

    private fun <T: Any> extractRule(clazz: KClass<T>) = clazz.java.getAnnotation(Rule::class.java)

    private fun allRules() = listOf(
        extractRule(MustRule::class),
        extractRule(ShouldRule::class),
        extractRule(MayRule::class),
        extractRule(HintRule::class)
    )
}
