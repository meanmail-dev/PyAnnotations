package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ReplacePipeUnionWithAnyQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for `X | Any` pattern in Python 3.10+ pipe union syntax.
 * Union with `Any` is equivalent to just `Any` since Any accepts all types.
 */
class PipeSyntaxWithAnyInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "X | Any can be simplified to 'Any'"
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
                if (hasType(node, "Any")) {
                    registerProblem(
                        node,
                        "Simplify ${node.text} to 'Any' - union with 'Any' is equivalent to 'Any'",
                        ReplacePipeUnionWithAnyQuickFix()
                    )
                } else {
                    visitElement(node)
                }
            }
        }
    }
}
