package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ConvertPipeToUnionQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection to convert modern pipe syntax X | Y to classic Union[X, Y].
 * Useful for projects targeting Python < 3.10.
 */
class ConvertPipeToUnionInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Pipe union can be converted to Union syntax (Python < 3.10)"
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
                val types = collectPipeUnionTypes(node)
                val unionSyntax = "Union[${types.joinToString(", ") { it.text }}]"

                registerProblem(
                    node,
                    "${node.text} can be converted to $unionSyntax for Python < 3.10 compatibility",
                    ConvertPipeToUnionQuickFix()
                )
            }
        }
    }
}
