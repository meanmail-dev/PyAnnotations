package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyParenthesizedExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.RemoveRedundantParenthesesQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for redundant parentheses around single type in pipe union.
 * For example: `(X) | Y` can be simplified to `X | Y`.
 */
class RedundantParenthesesInPipeUnionInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Redundant parentheses in pipe union"
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        val context = PyInspectionVisitor.getContext(session)
        return Visitor(holder, context)
    }

    companion object {
        class Visitor(
            holder: ProblemsHolder?,
            context: TypeEvalContext
        ) : BasePipeUnionVisitor(holder, context) {

            override fun visitPipeUnionExpression(node: PyBinaryExpression) {
                if (hasRedundantParentheses(node)) {
                    registerProblem(
                        node,
                        "Redundant parentheses around single type in union",
                        RemoveRedundantParenthesesQuickFix()
                    )
                } else {
                    visitElement(node)
                }
            }

            private fun hasRedundantParentheses(node: PyBinaryExpression): Boolean {
                return hasRedundantParens(node.leftExpression) || hasRedundantParens(node.rightExpression)
            }

            private fun hasRedundantParens(element: PsiElement?): Boolean {
                if (element !is PyParenthesizedExpression) return false
                val contained = element.containedExpression ?: return false
                // Parentheses around pipe union are NOT redundant (might be for grouping)
                // Only parentheses around single type are redundant
                return !(contained is PyBinaryExpression && contained.operator == PyTokenTypes.OR)
            }
        }
    }
}
