package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyTupleExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ConvertUnionToPipeSyntaxQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection to convert classic Union[X, Y] syntax to modern pipe syntax X | Y.
 * Only applicable for Python 3.10+.
 */
class ConvertUnionToModernSyntaxInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Union can be converted to pipe syntax (Python 3.10+)"
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

            override fun visitPyAnnotationUnionExpression(node: PyExpression, items: PyTupleExpression) {
                registerProblem(
                    node,
                    "Union[${items.text}] can be converted to ${items.elements.joinToString(" | ") { it.text }}",
                    ConvertUnionToPipeSyntaxQuickFix()
                )
            }
        }
    }
}
