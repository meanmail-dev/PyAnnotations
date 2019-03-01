package ru.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyExpression
import org.jetbrains.annotations.Nls
import ru.meanmail.quickfix.ReplaceUnionWithOneChildToChildQuickFix

class UnionWithOneChildToChildInspection : PyInspection() {
    @Nls
    override fun getDisplayName(): String {
        return "Union[item] instead item"
    }
    
    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean,
                              session: LocalInspectionToolSession): PsiElementVisitor {
        return Visitor(holder, session)
    }
    
    companion object {
        class Visitor(holder: ProblemsHolder?, session: LocalInspectionToolSession) :
                BaseInspectionVisitor(holder, session) {
            
            override fun visitPyAnnotationUnionWithOneChildExpression(node: PyExpression, item: PsiElement) {
                registerProblem(node, "Replace ${node.text} to ${item.text}",
                        ReplaceUnionWithOneChildToChildQuickFix())
            }
        }
    }
}
