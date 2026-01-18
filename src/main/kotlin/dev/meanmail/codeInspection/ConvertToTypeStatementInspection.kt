package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTargetExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ConvertToTypeStatementQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection to suggest converting TypeAlias to the new type statement syntax (Python 3.12+, PEP 695).
 * For example: MyType: TypeAlias = int -> type MyType = int
 */
class ConvertToTypeStatementInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Convert TypeAlias to type statement"
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

            override fun visitPyAssignmentStatement(node: PyAssignmentStatement) {
                val targets = node.targets
                if (targets.size != 1) return

                val target = targets[0] as? PyTargetExpression ?: return
                val annotation = target.annotation ?: return
                val annotationValue = annotation.value ?: return

                // Check if annotation is TypeAlias
                val isTypeAlias = when (annotationValue) {
                    is PyReferenceExpression -> annotationValue.text == "TypeAlias"
                    is PySubscriptionExpression -> {
                        val operand = annotationValue.operand
                        operand.text == "TypeAlias" || operand.text == "typing.TypeAlias"
                    }
                    else -> false
                }

                if (isTypeAlias) {
                    val aliasName = target.name ?: return
                    val assignedValue = node.assignedValue ?: return

                    registerProblem(
                        node,
                        "Can be converted to 'type $aliasName = ${assignedValue.text}' (Python 3.12+)",
                        ConvertToTypeStatementQuickFix()
                    )
                }
            }
        }
    }
}
