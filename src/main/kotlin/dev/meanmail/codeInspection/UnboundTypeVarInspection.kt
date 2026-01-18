package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.annotations.Nls

/**
 * Inspection for detecting TypeVar used in function annotations without being bound
 * to a generic class or used consistently across parameters/return type.
 */
class UnboundTypeVarInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Unbound TypeVar in annotation"
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
                // Skip methods in generic classes - they have bound TypeVars
                val containingClass = node.containingClass
                if (containingClass != null && isGenericClass(containingClass)) {
                    return
                }

                val typeVarUsages = mutableMapOf<String, MutableList<PyReferenceExpression>>()

                // Collect TypeVar usages from parameters
                for (param in node.parameterList.parameters) {
                    val namedParam = param as? PyNamedParameter ?: continue
                    val annotation = namedParam.annotation ?: continue
                    collectTypeVarReferences(annotation, typeVarUsages)
                }

                // Collect TypeVar usages from return type
                val returnAnnotation = node.annotation
                if (returnAnnotation != null) {
                    collectTypeVarReferences(returnAnnotation, typeVarUsages)
                }

                // Report TypeVars used only once (not bound to anything meaningful)
                for ((typeVarName, usages) in typeVarUsages) {
                    if (usages.size == 1) {
                        val usage = usages[0]
                        registerProblem(
                            usage,
                            "TypeVar '$typeVarName' is used only once and doesn't constrain types"
                        )
                    }
                }
            }

            private fun isGenericClass(pyClass: PyClass): Boolean {
                for (expr in pyClass.superClassExpressions) {
                    val text = expr.text
                    if (text.startsWith("Generic[") || text == "Generic") {
                        return true
                    }
                    if (expr is PySubscriptionExpression) {
                        val operand = expr.operand
                        if (operand is PyReferenceExpression && operand.referencedName == "Generic") {
                            return true
                        }
                    }
                }
                return false
            }

            private fun collectTypeVarReferences(
                element: PyAnnotation,
                usages: MutableMap<String, MutableList<PyReferenceExpression>>
            ) {
                val refs = PsiTreeUtil.findChildrenOfType(element, PyReferenceExpression::class.java)
                for (ref in refs) {
                    val name = ref.referencedName ?: continue
                    // Heuristic: TypeVars are typically single uppercase letters or end with "_T"
                    // or are explicitly defined with TypeVar()
                    if (isLikelyTypeVar(name)) {
                        usages.getOrPut(name) { mutableListOf() }.add(ref)
                    }
                }
            }

            private fun isLikelyTypeVar(name: String): Boolean {
                // Single uppercase letter (T, K, V, etc.)
                if (name.length == 1 && name[0].isUpperCase()) {
                    return true
                }
                // Ends with _T or _co or _contra (common TypeVar naming conventions)
                if (name.endsWith("_T") || name.endsWith("_co") || name.endsWith("_contra")) {
                    return true
                }
                // Starts with T and followed by digits or nothing (T1, T2)
                if (name.startsWith("T") && (name.length == 1 || name.substring(1).all { it.isDigit() })) {
                    return true
                }
                return false
            }
        }
    }
}
