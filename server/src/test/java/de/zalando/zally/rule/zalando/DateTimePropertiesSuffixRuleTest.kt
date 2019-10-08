package de.zalando.zally.rule.zalando

import de.zalando.zally.getOpenApiContextFromContent
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class DateTimePropertiesSuffixRuleTest {

    private val rule = DateTimePropertiesSuffixRule(testConfig)

    @Test
    fun `rule should pass with correct "date-time" fields`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Pet:
                  properties:
                    created_at:
                      type: string
                      format: date-time
                    modified_at:
                      type: string
                      format: date-time                      
                    occurred_at:
                      type: string
                      format: date-time                      
                    returned_at:
                      type: string
                      format: date-time
            """.trimIndent()
        val violations = rule.validate(getOpenApiContextFromContent(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `rule should pass with correct "date" fields`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created_at:
                      type: string
                      format: date
                    modified_at:
                      type: string
                      format: date                      
                    occurred_at:
                      type: string
                      format: date                      
                    returned_at:
                      type: string
                      format: date                  
            """.trimIndent()
        val violations = rule.validate(getOpenApiContextFromContent(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `rule should ignore fields unkown fields`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    described:
                      type: string
                      format: date
                    updated:
                      type: string
                      format: date-time                      
            """.trimIndent()
        val violations = rule.validate(getOpenApiContextFromContent(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should ignore expected fields with non-date and time types`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created:
                      type: string                      
                    occurred:
                      type: string                      
                    returned:
                      type: string                      
                    modified:
                      type: int                                          
            """.trimIndent()
        val violations = rule.validate(getOpenApiContextFromContent(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `rule should fail to validate schema`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created:
                      type: string
                      format: date-time
                    occurred:
                      type: string
                      format: date
                    returned:
                      type: string
                      format: date-time
                    modified:
                      type: string
                      format: date
            """.trimIndent()
        val violations = rule.validate(getOpenApiContextFromContent(content))
        assertThat(violations.map{it.description}).containsExactly(
            DateTimePropertiesSuffixRule.generateMessage("created", "string", "date-time"),
            DateTimePropertiesSuffixRule.generateMessage("occurred", "string", "date"),
            DateTimePropertiesSuffixRule.generateMessage("returned", "string", "date-time"),
            DateTimePropertiesSuffixRule.generateMessage("modified", "string", "date")
        )
    }
}
