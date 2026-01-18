package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTupleExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.SimplifyCallableQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for simplifying Callable type annotations.
 * For example: Callable[..., Any] can often be simplified to just Callable.
 */
class SimplifyCallableInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Simplify Callable annotation"
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
        ) : PyInspectionVisitor(holder, context) {

            override fun visitPySubscriptionExpression(node: PySubscriptionExpression) {
                val operand = node.operand
                val operandText = when (operand) {
                    is PyReferenceExpression -> operand.referencedName ?: operand.text
                    else -> operand.text
                }

                if (operandText != "Callable") return

                val indexExpression = node.indexExpression
                if (indexExpression !is PyTupleExpression) return

                val elements = indexExpression.elements
                if (elements.size != 2) return

                val paramsExpr = elements[0]
                val returnExpr = elements[1]

                // Check for Callable[..., Any] -> Callable
                if (paramsExpr.text == "..." && returnExpr.text == "Any") {
                    registerProblem(
                        node,
                        "Callable[..., Any] can be simplified to Callable",
                        SimplifyCallableQuickFix("Callable")
                    )
                    return
                }

                // Check for Callable[[...], None] where all params are Any
                if (paramsExpr is PyListLiteralExpression && returnExpr.text == "None") {
                    val params = paramsExpr.elements
                    if (params.isNotEmpty() && params.all { it.text == "Any" }) {
                        val paramCount = params.size
                        registerProblem(
                            node,
                            "Callable with all Any parameters could be simplified",
                            SimplifyCallableQuickFix("Callable[[${(1..paramCount).joinToString(", ") { "Any" }}], None]")
                        )
                    }
                }
            }
        }
    }
}
