package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ReplaceTypingCollectionQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for deprecated typing module collections (Python 3.9+).
 * For example: typing.List[int] should be list[int].
 *
 * Covered types:
 * - List -> list
 * - Dict -> dict
 * - Set -> set
 * - FrozenSet -> frozenset
 * - Tuple -> tuple
 * - Type -> type
 */
class DeprecatedTypingCollectionInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Deprecated typing collection"
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
        private val DEPRECATED_TYPES = mapOf(
            "List" to "list",
            "Dict" to "dict",
            "Set" to "set",
            "FrozenSet" to "frozenset",
            "Tuple" to "tuple",
            "Type" to "type"
        )

        class Visitor(
            holder: ProblemsHolder?,
            context: TypeEvalContext
        ) : PyInspectionVisitor(holder, context) {

            override fun visitPySubscriptionExpression(node: PySubscriptionExpression) {
                val operand = node.operand
                val typeName: String? = when (operand) {
                    is PyReferenceExpression -> {
                        val qualifier = operand.qualifier
                        if (qualifier != null && qualifier.text == "typing") {
                            operand.referencedName
                        } else {
                            operand.text
                        }
                    }
                    else -> operand.text
                }

                if (typeName == null) return

                val replacement = DEPRECATED_TYPES[typeName]
                if (replacement != null) {
                    registerProblem(
                        node,
                        "Use '$replacement' instead of '$typeName' (Python 3.9+)",
                        ReplaceTypingCollectionQuickFix(typeName, replacement)
                    )
                }
            }
        }
    }
}
