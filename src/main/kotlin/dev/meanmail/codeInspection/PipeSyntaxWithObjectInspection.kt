package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ReplacePipeUnionWithObjectQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for `X | object` pattern in Python 3.10+ pipe union syntax.
 * Union with `object` is equivalent to just `object` since object is the base of all types.
 */
class PipeSyntaxWithObjectInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "X | object can be simplified to 'object'"
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
                if (hasType(node, "object") && !hasType(node, "None", "NoneType")) {
                    registerProblem(
                        node,
                        "Simplify ${node.text} to 'object' - union with 'object' is equivalent to 'object'",
                        ReplacePipeUnionWithObjectQuickFix()
                    )
                } else {
                    visitElement(node)
                }
            }
        }
    }
}
