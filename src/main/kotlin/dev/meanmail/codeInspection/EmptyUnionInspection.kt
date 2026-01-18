package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTupleExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.annotations.Nls

/**
 * Inspection for detecting empty or invalid Union types.
 * For example: Union[] or Union[()] are invalid.
 */
class EmptyUnionInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Empty or invalid Union"
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
                val operandName = when (operand) {
                    is PyReferenceExpression -> operand.referencedName
                    else -> operand.text
                }

                if (operandName != "Union") return

                val indexExpression = node.indexExpression

                when {
                    // Union without any index: Union[]
                    indexExpression == null -> {
                        registerProblem(
                            node,
                            "Empty Union is invalid"
                        )
                    }
                    // Union with empty tuple: Union[()]
                    indexExpression is PyTupleExpression && indexExpression.elements.isEmpty() -> {
                        registerProblem(
                            node,
                            "Union[()] is invalid - Union requires at least one type"
                        )
                    }
                    // Union with single empty element
                    indexExpression.text == "()" -> {
                        registerProblem(
                            node,
                            "Union[()] is invalid - Union requires at least one type"
                        )
                    }
                }
            }
        }
    }
}
