package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.getAllProperties
import io.swagger.v3.oas.models.media.Schema

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "235",
    severity = Severity.SHOULD,
    title = "Name date/time properties using the \"_at\" suffix"
)
class DateTimePropertiesSuffixRule(rulesConfig: Config) {

    private val propertyNames = rulesConfig.getStringList("${javaClass.simpleName}.properties_to_check").toHashSet()

    private val propertyFormats = setOf("date", "date-time")

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> {
        return context.api.getAllProperties().map {
            val schema = it.value
            val result = checkProperty(it.key, schema)
            result?.let { context.violation(result, schema) }
        }.filterNotNull()
    }

    private fun checkProperty(name: String, schema: Schema<Any>): String? {
        if (schema.type == "string" && name in propertyNames && schema.format in propertyFormats) {
            return generateMessage(name, schema.type, schema.format)
        }
        return null
    }

    companion object {
        fun generateMessage(name: String, type: String, format: String) =
            """Property "$name" of type "$type" and format "$format" should have suffix "_at" e.g. "${name}_at""""
    }
}
