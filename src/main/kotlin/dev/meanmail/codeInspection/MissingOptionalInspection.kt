package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyNamedParameter
import com.jetbrains.python.psi.PyNoneLiteralExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.AddOptionalQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for parameters with None default but missing Optional in annotation.
 * For example: def foo(x: int = None) should be def foo(x: Optional[int] = None).
 */
class MissingOptionalInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Missing Optional for None default"
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

            override fun visitPyFunction(node: PyFunction) {
                for (param in node.parameterList.parameters) {
                    val namedParam = param as? PyNamedParameter ?: continue
                    val defaultValue = namedParam.defaultValue ?: continue
                    val annotation = namedParam.annotation ?: continue

                    // Check if default is None
                    if (defaultValue !is PyNoneLiteralExpression) continue

                    val annotationText = annotation.text

                    // Skip if already Optional or already contains None
                    if (annotationText.startsWith("Optional[")) continue
                    if (annotationText.contains("| None")) continue
                    if (annotationText.contains("None |")) continue
                    if (annotationText == "None") continue

                    // Skip if it's Union with None
                    if (annotationText.startsWith("Union[") && annotationText.contains("None")) continue

                    registerProblem(
                        annotation,
                        "Parameter has None default but annotation is not Optional",
                        AddOptionalQuickFix()
                    )
                }
            }
        }
    }
}
