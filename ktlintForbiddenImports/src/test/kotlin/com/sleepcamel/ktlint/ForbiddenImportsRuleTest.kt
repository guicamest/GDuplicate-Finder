package com.sleepcamel.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ForbiddenImportsRuleTest {

    private val wrappingRuleAssertThat = assertThatRule { ForbiddenImportsRule() }

    @Test
    fun `import of java-io-File should report a violation`() {
        val fileContent = """
            import java.io.File
        """.trimIndent()
        System.setProperty("ktlintDebug", "ast")
        wrappingRuleAssertThat(fileContent)
            .hasLintViolationWithoutAutoCorrect(
                line = 1,
                col = 1,
                detail = "no-java-io-file-import"
            )
    }
}
