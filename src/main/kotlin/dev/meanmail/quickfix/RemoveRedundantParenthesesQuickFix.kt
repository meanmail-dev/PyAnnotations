package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyParenthesizedExpression

class RemoveRedundantParenthesesQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Remove redundant parentheses"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PyBinaryExpression
        if (union == null || !union.isValid) {
            LOG.warn("Invalid PSI element for parentheses removal")
            return
        }

        val simplifiedText = buildSimplifiedText(union)
        if (simplifiedText == null) {
            LOG.warn("Could not build simplified text")
            return
        }

        val elementGenerator = PyElementGenerator.getInstance(project)
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON310, simplifiedText))
    }

    private fun buildSimplifiedText(node: PyBinaryExpression): String? {
        val parts = mutableListOf<String>()

        fun collect(element: PsiElement?) {
            when (element) {
                is PyBinaryExpression -> {
                    if (element.operator == PyTokenTypes.OR) {
                        collect(element.leftExpression)
                        collect(element.rightExpression)
                    } else {
                        parts.add(element.text)
                    }
                }
                is PyParenthesizedExpression -> {
                    val contained = element.containedExpression
                    if (contained is PyBinaryExpression && contained.operator == PyTokenTypes.OR) {
                        // Keep nested unions as-is (handled by NestedPipeUnionInspection)
                        parts.add(element.text)
                    } else {
                        // Remove redundant parentheses around single type
                        parts.add(contained?.text ?: element.text)
                    }
                }
                null -> {}
                else -> parts.add(element.text)
            }
        }

        collect(node)
        return if (parts.isNotEmpty()) parts.joinToString(" | ") else null
    }

    companion object {
        private val LOG = Logger.getInstance(RemoveRedundantParenthesesQuickFix::class.java)
    }
}
