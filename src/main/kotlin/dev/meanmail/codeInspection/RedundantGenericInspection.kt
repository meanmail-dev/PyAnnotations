package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.RemoveRedundantGenericQuickFix
import org.jetbrains.annotations.Nls

/**
 * Inspection for detecting redundant Generic[T] in class definitions.
 * When a class inherits from a parameterized generic type, explicitly
 * including Generic[T] is redundant.
 */
class RedundantGenericInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Redundant Generic in class definition"
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
        private val GENERIC_TYPES = setOf(
            "List", "Dict", "Set", "FrozenSet", "Tuple",
            "Sequence", "MutableSequence", "Mapping", "MutableMapping",
            "Iterable", "Iterator", "Collection", "Container",
            "list", "dict", "set", "frozenset", "tuple"
        )

        class Visitor(
            holder: ProblemsHolder?,
            context: TypeEvalContext
        ) : PyInspectionVisitor(holder, context) {

            override fun visitPyClass(node: PyClass) {
                val superClassExpressions = node.superClassExpressions
                if (superClassExpressions.size < 2) return

                var hasGeneric = false
                var genericExpr: PySubscriptionExpression? = null
                var hasOtherGenericBase = false

                for (expr in superClassExpressions) {
                    when (expr) {
                        is PySubscriptionExpression -> {
                            val operand = expr.operand
                            val operandName = when (operand) {
                                is PyReferenceExpression -> operand.referencedName
                                else -> operand.text
                            }

                            if (operandName == "Generic") {
                                hasGeneric = true
                                genericExpr = expr
                            } else if (operandName in GENERIC_TYPES) {
                                hasOtherGenericBase = true
                            }
                        }
                        is PyReferenceExpression -> {
                            val name = expr.referencedName
                            if (name in GENERIC_TYPES) {
                                hasOtherGenericBase = true
                            }
                        }
                    }
                }

                if (hasGeneric && hasOtherGenericBase && genericExpr != null) {
                    registerProblem(
                        genericExpr,
                        "Generic is redundant when inheriting from parameterized generic types",
                        RemoveRedundantGenericQuickFix()
                    )
                }
            }
        }
    }
}
