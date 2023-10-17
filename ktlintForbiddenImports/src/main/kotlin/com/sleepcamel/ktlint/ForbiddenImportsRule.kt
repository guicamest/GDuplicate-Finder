package com.sleepcamel.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class ForbiddenImportsRule : Rule(
    ruleId = RuleId("$CUSTOM_RULE_SET_ID:forbidden"),
    about = About()
) {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == ElementType.IMPORT_LIST) {
            val children = node.getChildren(null)
            if (children.isNotEmpty()) {
                children.forEach { println(it) }
                val first = children.first()
                println(first)
            }
//            if (node is LeafPsiElement && node.textMatches("var") &&
//            getNonStrictParentOfType(node, KtStringTemplateEntry::class.java) == null
//        ) {
//            emit(node.startOffset, "Unexpected var, use val instead", false)
        }
    }
}
