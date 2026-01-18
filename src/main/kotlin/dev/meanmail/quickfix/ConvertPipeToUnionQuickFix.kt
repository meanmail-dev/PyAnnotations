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

class ConvertPipeToUnionQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Convert to Union syntax (Python 3.5+)"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val pipeUnion = descriptor.psiElement as? PyBinaryExpression
        if (pipeUnion == null || !pipeUnion.isValid) {
            LOG.warn("Invalid PSI element for pipe to Union conversion")
            return
        }

        val types = collectTypes(pipeUnion)
        if (types.isEmpty()) {
            LOG.warn("No types collected from pipe union")
            return
        }

        val unionSyntax = "Union[${types.joinToString(", ") { it.text }}]"
        val elementGenerator = PyElementGenerator.getInstance(project)
        pipeUnion.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, unionSyntax))
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
                    val contained = el.containedExpression
                    if (contained is PyBinaryExpression && contained.operator == PyTokenTypes.OR) {
                        collect(contained)
                    } else {
                        types.add(contained ?: el)
                    }
                }
                null -> {}
                else -> types.add(el)
            }
        }

        collect(element)
        return types
    }

    companion object {
        private val LOG = Logger.getInstance(ConvertPipeToUnionQuickFix::class.java)
    }
}
