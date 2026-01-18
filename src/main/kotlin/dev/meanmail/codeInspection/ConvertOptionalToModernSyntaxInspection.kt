package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ConvertOptionalToPipeSyntaxQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection to convert classic Optional[X] syntax to modern pipe syntax X | None.
 * Only applicable for Python 3.10+.
 */
class ConvertOptionalToModernSyntaxInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Optional can be converted to pipe syntax (Python 3.10+)"
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
        ) : BaseInspectionVisitor(holder, context) {

            override fun visitPySubscriptionExpression(node: PySubscriptionExpression) {
                if (node.firstChild?.text == "Optional" && node.children.size >= 2) {
                    val innerType = node.children.getOrNull(1)
                    if (innerType != null) {
                        registerProblem(
                            node,
                            "Optional[${innerType.text}] can be converted to ${innerType.text} | None",
                            ConvertOptionalToPipeSyntaxQuickFix()
                        )
                    }
                } else {
                    super.visitPySubscriptionExpression(node)
                }
            }
        }
    }
}
