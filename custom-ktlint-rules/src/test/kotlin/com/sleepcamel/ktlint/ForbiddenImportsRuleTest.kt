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

        wrappingRuleAssertThat(fileContent)
            .hasLintViolationWithoutAutoCorrect(
                line = 1,
                col = 1,
                detail = "File has a forbidden import: java.io.File"
            )
    }

    @Test
    fun `import of java-io-File with alias should report a violation`() {
        val fileContent = """
            import java.io.File as MyFile
        """.trimIndent()

        wrappingRuleAssertThat(fileContent)
            .hasLintViolationWithoutAutoCorrect(
                line = 1,
                col = 1,
                detail = "File has a forbidden import: java.io.File"
            )
    }

    @Test
    fun `import of java-nio-file should not report a violation`() {
        val fileContent = """
            import java.nio.file.*
        """.trimIndent()

        wrappingRuleAssertThat(fileContent).hasNoLintViolations()
    }
}
