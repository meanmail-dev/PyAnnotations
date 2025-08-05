package ru.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.annotations.Nls
import ru.meanmail.quickfix.ReplaceUnionWithOneChildToChildQuickFix

class UnionWithOneChildToChildInspection : PyInspection() {
    @Nls
    override fun getDisplayName(): String {
        return "Union[item] instead item"
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

            override fun visitPyAnnotationUnionWithOneChildExpression(node: PyExpression, item: PsiElement) {
                registerProblem(
                    node, "Simplify ${node.text} to ${item.text} - Union with a single type is redundant",
                    ReplaceUnionWithOneChildToChildQuickFix()
                )
            }
        }
    }
}
