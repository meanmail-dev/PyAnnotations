package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression

class ReplaceUnionWithNoneToOptionalUnionQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Replace Union[None, ...] to Optional[Union[...]]"
    }

    private val pattern = "('|\"|)(None|NoneType)\\1".toRegex()

    private fun matchNone(node: PsiElement): Boolean {
        return pattern.matches(node.text)
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PySubscriptionExpression
        if (union == null || !union.isValid) {
            LOG.warn("Invalid PSI element for Union replacement")
            return
        }

        val indexExpression = union.children.getOrNull(1)
        if (indexExpression == null || !indexExpression.isValid) {
            LOG.warn("Missing index expression in Union")
            return
        }

        val elementGenerator = PyElementGenerator.getInstance(project)

        val noneElements = indexExpression.children.filter { matchNone(it) }
        noneElements.forEach { element ->
            if (element.isValid) {
                element.delete()
            }
        }

        val remainingChildren = indexExpression.children
        val replacer = when {
            remainingChildren.isEmpty() -> {
                LOG.warn("No remaining children after removing None")
                return
            }
            remainingChildren.size == 1 -> "Optional[${remainingChildren[0].text}]"
            else -> "Optional[${union.text}]"
        }

        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, replacer))
    }

    companion object {
        private val LOG = Logger.getInstance(ReplaceUnionWithNoneToOptionalUnionQuickFix::class.java)
    }
}
