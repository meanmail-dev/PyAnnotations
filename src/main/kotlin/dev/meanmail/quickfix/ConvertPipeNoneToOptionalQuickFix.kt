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

class ConvertPipeNoneToOptionalQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Convert to Optional syntax (Python 3.5+)"
    }

    private val nonePattern = "('|\"|)(None|NoneType)\\1".toRegex()

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val pipeUnion = descriptor.psiElement as? PyBinaryExpression
        if (pipeUnion == null || !pipeUnion.isValid) {
            LOG.warn("Invalid PSI element for pipe to Optional conversion")
            return
        }

        val types = collectTypes(pipeUnion)
        val nonNoneTypes = types.filter { !nonePattern.matches(it.text) }

        if (nonNoneTypes.isEmpty()) {
            LOG.warn("No non-None types found")
            return
        }

        val optionalSyntax = if (nonNoneTypes.size == 1) {
            "Optional[${nonNoneTypes[0].text}]"
        } else {
            "Optional[Union[${nonNoneTypes.joinToString(", ") { it.text }}]]"
        }

        val elementGenerator = PyElementGenerator.getInstance(project)
        pipeUnion.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, optionalSyntax))
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
        private val LOG = Logger.getInstance(ConvertPipeNoneToOptionalQuickFix::class.java)
    }
}
