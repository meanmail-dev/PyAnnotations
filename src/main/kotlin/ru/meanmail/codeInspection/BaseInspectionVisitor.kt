package ru.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTupleExpression


open class BaseInspectionVisitor(holder: ProblemsHolder?, session: LocalInspectionToolSession) : PyInspectionVisitor(holder, session) {
    open fun visitPyAnnotationUnionExpression(node: PyExpression, items: PyTupleExpression) {
        visitElement(node)
    }
    
    open fun visitPyAnnotationUnionWithOneChildExpression(node: PyExpression, item: PsiElement) {
        visitElement(node)
    }
    
    override fun visitPySubscriptionExpression(node: PySubscriptionExpression) {
        if (node.firstChild.text == "Union" && node.children.count() == 2) {
            val secondChild = node.children[1]
            if (secondChild  as? PyTupleExpression != null) {
                visitPyAnnotationUnionExpression(node, secondChild)
            } else {
                visitPyAnnotationUnionWithOneChildExpression(node, secondChild)
            }
        } else {
            visitElement(node)
        }
    }
    
    fun hasChildren(node: PyExpression, text: String): Boolean {
        val pattern = "('|\"|)($text)\\1".toRegex()
        return node.children.any {
            pattern.matches(it.text)
        }
    }
}
