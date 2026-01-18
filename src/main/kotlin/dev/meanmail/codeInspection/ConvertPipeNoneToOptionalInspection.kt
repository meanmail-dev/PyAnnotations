package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ConvertPipeNoneToOptionalQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection to convert modern pipe syntax X | None to classic Optional[X].
 * Useful for projects targeting Python < 3.10.
 */
class ConvertPipeNoneToOptionalInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "X | None can be converted to Optional (Python < 3.10)"
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
        private val nonePattern = "('|\"|)(None|NoneType)\\1".toRegex()

        class Visitor(
            holder: ProblemsHolder?,
            context: TypeEvalContext
        ) : BasePipeUnionVisitor(holder, context) {

            override fun visitPipeUnionExpression(node: PyBinaryExpression) {
                if (!hasType(node, "None", "NoneType")) {
                    visitElement(node)
                    return
                }

                val types = collectPipeUnionTypes(node)
                val nonNoneTypes = types.filter { !nonePattern.matches(it.text) }

                val optionalSyntax = if (nonNoneTypes.size == 1) {
                    "Optional[${nonNoneTypes[0].text}]"
                } else {
                    "Optional[Union[${nonNoneTypes.joinToString(", ") { it.text }}]]"
                }

                registerProblem(
                    node,
                    "${node.text} can be converted to $optionalSyntax for Python < 3.10 compatibility",
                    ConvertPipeNoneToOptionalQuickFix()
                )
            }
        }
    }
}
