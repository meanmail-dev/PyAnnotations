package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.annotations.Nls

/**
 * Inspection for checking Protocol method signatures.
 * Ensures Protocol methods have proper type annotations.
 */
class ProtocolMethodAnnotationInspection : PyInspection() {

    @Nls
    override fun getDisplayName(): String {
        return "Protocol method without type annotations"
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

            override fun visitPyClass(node: PyClass) {
                if (!isProtocolClass(node)) return

                for (method in node.methods) {
                    checkMethodAnnotations(method)
                }
            }

            private fun isProtocolClass(pyClass: PyClass): Boolean {
                for (expr in pyClass.superClassExpressions) {
                    val text = expr.text
                    if (text == "Protocol" || text == "typing.Protocol" ||
                        text.startsWith("Protocol[") || text.startsWith("typing.Protocol[")) {
                        return true
                    }
                    if (expr is PyReferenceExpression && expr.referencedName == "Protocol") {
                        return true
                    }
                    if (expr is PySubscriptionExpression) {
                        val operand = expr.operand
                        if (operand is PyReferenceExpression &&
                            (operand.referencedName == "Protocol" || operand.text == "typing.Protocol")) {
                            return true
                        }
                    }
                }
                return false
            }

            private fun checkMethodAnnotations(method: PyFunction) {
                // Skip dunder methods except __init__ and __call__
                val methodName = method.name ?: return
                if (methodName.startsWith("__") && methodName.endsWith("__") &&
                    methodName != "__init__" && methodName != "__call__") {
                    return
                }

                // Check return type annotation (except for __init__)
                if (methodName != "__init__" && method.annotation == null) {
                    registerProblem(
                        method.nameIdentifier ?: method,
                        "Protocol method '$methodName' should have a return type annotation"
                    )
                }

                // Check parameter annotations
                val params = method.parameterList.parameters
                for (param in params) {
                    val namedParam = param as? PyNamedParameter ?: continue
                    val paramName = namedParam.name ?: continue

                    // Skip self/cls
                    if (paramName == "self" || paramName == "cls") continue

                    if (namedParam.annotation == null) {
                        registerProblem(
                            namedParam,
                            "Protocol method parameter '$paramName' should have a type annotation"
                        )
                    }
                }
            }
        }
    }
}
