package de.zalando.zally.rule

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RulesPolicy(@Value("\${zally.ignoreRules:}") val ignoreRules: Array<String>, val minSeverity: Severity = Severity.MUST) {

    fun accepts(rule: Rule): Boolean {
        return !ignoreRules.contains(rule.id) && acceptsSeverity(rule.severity)
    }

    private fun acceptsSeverity(severity: Severity): Boolean = severity >= minSeverity

    fun withMoreIgnores(moreIgnores: List<String>) = RulesPolicy(ignoreRules + moreIgnores)

    fun withMinSeverity(severity: Severity) = RulesPolicy(ignoreRules, severity)
}
