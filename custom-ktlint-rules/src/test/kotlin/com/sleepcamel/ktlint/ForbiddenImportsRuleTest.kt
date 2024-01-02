/*
 * Copyright 2012-2024 guicamest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sleepcamel.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ForbiddenImportsRuleTest {
    private val wrappingRuleAssertThat = assertThatRule { ForbiddenImportsRule() }

    @Test
    fun `import of java-io-File should report a violation`() {
        val fileContent =
            """
            import java.io.File
            """.trimIndent()

        wrappingRuleAssertThat(fileContent)
            .hasLintViolationWithoutAutoCorrect(
                line = 1,
                col = 1,
                detail = "File has a forbidden import: java.io.File",
            )
    }

    @Test
    fun `import of java-io-File with alias should report a violation`() {
        val fileContent =
            """
            import java.io.File as MyFile
            """.trimIndent()

        wrappingRuleAssertThat(fileContent)
            .hasLintViolationWithoutAutoCorrect(
                line = 1,
                col = 1,
                detail = "File has a forbidden import: java.io.File",
            )
    }

    @Test
    fun `import of java-nio-file should not report a violation`() {
        val fileContent =
            """
            import java.nio.file.*
            """.trimIndent()

        wrappingRuleAssertThat(fileContent).hasNoLintViolations()
    }
}
