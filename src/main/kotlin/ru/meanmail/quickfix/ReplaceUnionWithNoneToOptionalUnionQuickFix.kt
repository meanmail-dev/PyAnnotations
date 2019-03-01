package ru.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
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
        val union = descriptor.psiElement as? PySubscriptionExpression ?: return
        val elementGenerator = PyElementGenerator.getInstance(project)
        union.children[1].children.filter { matchNone(it) }.forEach { it.delete() }
        val replacer = if (union.children[1].children.count() == 1) {
            "Optional[${union.children[1].children[0].text}]"
        } else {
            "Optional[${union.text}]"
        }
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, replacer))
    }
}
