package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyTupleExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.RemoveRedundantUnionTypesQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for detecting redundant types in Union where one type is a subtype of another.
 * For example: Union[int, bool] -> int (because bool is a subtype of int).
 */
class RedundantUnionInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Redundant types in Union (subtype relationship)"
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
        // Map of type -> its supertypes
        private val SUBTYPE_RELATIONS = mapOf(
            "bool" to setOf("int", "float", "complex"),
            "int" to setOf("float", "complex"),
            "float" to setOf("complex"),
            "bytearray" to setOf("bytes"),
        )

        class Visitor(
            holder: ProblemsHolder?,
            context: TypeEvalContext
        ) : BaseInspectionVisitor(holder, context) {

            override fun visitPyAnnotationUnionExpression(node: PyExpression, items: PyTupleExpression) {
                val typeTexts = items.elements.map { it.text }
                val redundantTypes = mutableSetOf<String>()

                for (type in typeTexts) {
                    val supertypes = SUBTYPE_RELATIONS[type] ?: continue
                    for (supertype in supertypes) {
                        if (supertype in typeTexts) {
                            redundantTypes.add(type)
                            break
                        }
                    }
                }

                if (redundantTypes.isNotEmpty()) {
                    val supertype = typeTexts.first { it !in redundantTypes }
                    registerProblem(
                        node,
                        "Union contains redundant types: ${redundantTypes.joinToString(", ")} (subtype of $supertype)",
                        RemoveRedundantUnionTypesQuickFix(redundantTypes)
                    )
                } else {
                    visitElement(node)
                }
            }
        }
    }
}
