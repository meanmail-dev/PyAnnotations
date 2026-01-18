package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTupleExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.FlattenNestedUnionQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for nested Union types.
 * For example: Union[Union[int, str], float] should be Union[int, str, float].
 */
class NestedUnionInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Nested Union types"
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
                val hasNestedUnion = items.elements.any { element ->
                    (element as? PySubscriptionExpression)?.firstChild?.text == "Union"
                }

                if (hasNestedUnion) {
                    registerProblem(
                        node,
                        "Union contains nested Union types that can be flattened",
                        FlattenNestedUnionQuickFix()
                    )
                } else {
                    visitElement(node)
                }
            }
        }
    }
}
