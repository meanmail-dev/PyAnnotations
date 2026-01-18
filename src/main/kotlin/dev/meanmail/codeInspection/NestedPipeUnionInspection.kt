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
import dev.meanmail.quickfix.FlattenPipeUnionQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for nested pipe unions like `(X | Y) | Z` in Python 3.10+.
 * These can be flattened to `X | Y | Z`.
 */
class NestedPipeUnionInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Nested pipe union can be flattened"
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
                if (hasNestedPipeUnion(node)) {
                    registerProblem(
                        node,
                        "Nested pipe union can be flattened",
                        FlattenPipeUnionQuickFix()
                    )
                } else {
                    visitElement(node)
                }
            }

            private fun hasNestedPipeUnion(node: PyBinaryExpression): Boolean {
                return hasNestedUnionIn(node.leftExpression) || hasNestedUnionIn(node.rightExpression)
            }

            private fun hasNestedUnionIn(element: PsiElement?): Boolean {
                return when (element) {
                    is PyParenthesizedExpression -> {
                        val contained = element.containedExpression
                        contained is PyBinaryExpression && contained.operator == PyTokenTypes.OR
                    }
                    else -> false
                }
            }
        }
    }
}
