package de.zalando.zally.rule.zalando

import de.zalando.zally.getContextFromFixture
import de.zalando.zally.rule.ContentParseResult
import de.zalando.zally.rule.ContentParseResultAssert.Companion.assertThat
import de.zalando.zally.rule.DefaultContextFactory
import de.zalando.zally.rule.ZallyAssertions.assertThat
import de.zalando.zally.rule.api.Context
import de.zalando.zally.testConfig
import org.junit.Test

class PluralizeResourceNamesRuleTest {

    private val rule = PluralizeResourceNamesRule(testConfig)

    @Test
    fun positiveCase() {
        val context = getContextFromFixture("pluralizeResourcesValid.json")
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun negativeCase() {
        val context = getContextFromFixture("pluralizeResourcesInvalid.json")
        val violations = rule.validate(context)
        assertThat(violations)
            .descriptionsEqualTo(
                "Resource 'pet' appears to be singular",
                "Resource 'tail' appears to be singular"
            )
            .pointersEqualTo(
                "/paths/~1pet~1cats",
                "/paths/~1pets~1cats~1{cat-id}~1tail~1{tail-id}~1strands"
            )
    }

    @Test
    fun positiveCaseSpp() {
        val context = getContextFromFixture("api_spp.json")
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun positiveCasePathsWithTheApiPrefix() {
        val context = getContextFromFixture("spp_with_paths_having_api_prefix.json")
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun positiveCaseNoMustViolations() {
        val context = getContextFromFixture("no_must_violations.yaml")
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun negativeCaseTinbox() {
        val context = getContextFromFixture("api_tinbox.yaml")
        val violations = rule.validate(context)
        assertThat(violations)
            .pointersEqualTo(
                "/paths/~1meta~1article_domains",
                "/paths/~1meta~1colors",
                "/paths/~1meta~1commodity_groups",
                "/paths/~1meta~1size_grids",
                "/paths/~1meta~1tags",
                "/paths/~1queue~1configs~1{config-id}",
                "/paths/~1queue~1models",
                "/paths/~1queue~1models~1{model-id}",
                "/paths/~1queue~1summaries"
            )
    }

    @Test
    fun `validate with plurals returns no violations`() {
        val context = openApiContextWithPath("/plurals/things")

        val violations = rule.validate(context)

        assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `validate with singulars returns violations`() {
        val context = openApiContextWithPath("/singular/thing")

        val violations = rule.validate(context)

        assertThat(violations)
            .descriptionsEqualTo(
                "Resource 'singular' appears to be singular",
                "Resource 'thing' appears to be singular"
            )
            .pointersEqualTo(
                "/paths/~1singular~1thing",
                "/paths/~1singular~1thing"
            )
    }

    @Test
    fun `validate with parameters returns other violations`() {
        val context = openApiContextWithPath("/prefix/{parameter}/suffix")

        val violations = rule.validate(context)

        assertThat(violations)
            .descriptionsEqualTo(
                "Resource 'prefix' appears to be singular",
                "Resource 'suffix' appears to be singular"
            )
            .pointersEqualTo(
                "/paths/~1prefix~1{parameter}~1suffix",
                "/paths/~1prefix~1{parameter}~1suffix"
            )
    }

    @Test
    fun `validate with configured whitelisted singulars returns no violations`() {
        val context = openApiContextWithPath("/api/things")

        val violations = rule.validate(context)

        assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `validate with whitelisted prefix returns no violations`() {
        val context = openApiContextWithPath("/prefix/singular")
        rule.whitelist += "^/?prefix/.*".toRegex()

        val violations = rule.validate(context)

        assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `validate with whitelisted suffix returns no violations`() {
        val context = openApiContextWithPath("/singular/suffix")
        rule.whitelist += ".*/suffix/?${'$'}".toRegex()

        val violations = rule.validate(context)

        assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `validate with whitelisted component returns other violations`() {
        val context = openApiContextWithPath("/prefix/whitelisted/suffix")
        rule.whitelist += "/whitelisted/".toRegex()

        val violations = rule.validate(context)

        assertThat(violations)
            .descriptionsEqualTo(
                "Resource 'prefix' appears to be singular",
                "Resource 'suffix' appears to be singular"
            )
            .pointersEqualTo(
                "/paths/~1prefix~1whitelisted~1suffix",
                "/paths/~1prefix~1whitelisted~1suffix"
            )
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun openApiContextWithPath(path: String): Context {
        val content = """
                openapi: 3.0.0
                paths:
                  $path: {}
                """.trimIndent()
        val result = DefaultContextFactory().createOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        return (result as ContentParseResult.ParsedSuccessfully).result
    }
}
