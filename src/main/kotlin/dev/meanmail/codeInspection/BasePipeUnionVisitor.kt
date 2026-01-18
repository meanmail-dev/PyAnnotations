package dev.meanmail.codeInspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAnnotation
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.types.TypeEvalContext

/**
 * Base visitor for pipe union syntax (X | Y) inspections.
 * Only processes binary expressions with | operator in type annotation context.
 */
open class BasePipeUnionVisitor(
    holder: ProblemsHolder?,
    context: TypeEvalContext
) : PyInspectionVisitor(holder, context) {

    /**
     * Called when a pipe union expression is found in annotation context.
     * Override this to implement specific inspection logic.
     */
    open fun visitPipeUnionExpression(node: PyBinaryExpression) {
        visitElement(node)
    }

    override fun visitPyBinaryExpression(node: PyBinaryExpression) {
        if (node.operator == PyTokenTypes.OR && isInAnnotationContext(node)) {
            visitPipeUnionExpression(node)
        } else {
            visitElement(node)
        }
    }

    /**
     * Check if the expression is in a type annotation context.
     */
    private fun isInAnnotationContext(element: PsiElement): Boolean {
        var current: PsiElement? = element.parent
        while (current != null) {
            when (current) {
                is PyAnnotation -> return true
                is PyFunction -> {
                    // Check if we're in return type annotation
                    if (current.annotation?.value?.let { isAncestor(it, element) } == true) {
                        return true
                    }
                    return false
                }
            }
            current = current.parent
        }
        return false
    }

    private fun isAncestor(ancestor: PsiElement, element: PsiElement): Boolean {
        var current: PsiElement? = element
        while (current != null) {
            if (current == ancestor) return true
            current = current.parent
        }
        return false
    }

    /**
     * Collect all types from a pipe union chain (handles nested pipes).
     * For `X | Y | Z` returns [X, Y, Z]
     */
    protected fun collectPipeUnionTypes(node: PyBinaryExpression): List<PsiElement> {
        val types = mutableListOf<PsiElement>()

        fun collect(element: PsiElement?) {
            when {
                element is PyBinaryExpression && element.operator == PyTokenTypes.OR -> {
                    collect(element.leftExpression)
                    collect(element.rightExpression)
                }
                element != null -> types.add(element)
            }
        }

        collect(node)
        return types
    }

    /**
     * Check if any type in the pipe union matches the given pattern.
     */
    protected fun hasType(node: PyBinaryExpression, vararg typeNames: String): Boolean {
        val pattern = "('|\"|)(${typeNames.joinToString("|")})\\1".toRegex()
        return collectPipeUnionTypes(node).any { pattern.matches(it.text) }
    }
}
