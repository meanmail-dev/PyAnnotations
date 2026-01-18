package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyParenthesizedExpression

class FlattenPipeUnionQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Flatten nested pipe union"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PyBinaryExpression
        if (union == null || !union.isValid) {
            LOG.warn("Invalid PSI element for pipe union flattening")
            return
        }

        val types = collectTypes(union)
        if (types.isEmpty()) {
            LOG.warn("No types collected from pipe union")
            return
        }

        val flattenedText = types.joinToString(" | ") { it.text }
        val elementGenerator = PyElementGenerator.getInstance(project)
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON310, flattenedText))
    }

    private fun collectTypes(element: PsiElement): List<PsiElement> {
        val types = mutableListOf<PsiElement>()

        fun collect(el: PsiElement?) {
            when (el) {
                is PyBinaryExpression -> {
                    if (el.operator == PyTokenTypes.OR) {
                        collect(el.leftExpression)
                        collect(el.rightExpression)
                    } else {
                        types.add(el)
                    }
                }
                is PyParenthesizedExpression -> {
                    collect(el.containedExpression)
                }
                null -> {}
                else -> types.add(el)
            }
        }

        collect(element)
        return types
    }

    companion object {
        private val LOG = Logger.getInstance(FlattenPipeUnionQuickFix::class.java)
    }
}
