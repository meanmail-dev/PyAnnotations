package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyTupleExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.RemoveDuplicateTypesQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for duplicate types in Union.
 * For example: Union[int, int, str] should be Union[int, str].
 */
class DuplicateTypesInUnionInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Duplicate types in Union"
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
                val typeTexts = items.elements.map { it.text }
                val uniqueTypes = typeTexts.distinct()

                if (uniqueTypes.size < typeTexts.size) {
                    val duplicates = typeTexts.groupBy { it }
                        .filter { it.value.size > 1 }
                        .keys

                    registerProblem(
                        node,
                        "Union contains duplicate types: ${duplicates.joinToString(", ")}",
                        RemoveDuplicateTypesQuickFix()
                    )
                } else {
                    visitElement(node)
                }
            }
        }
    }
}
