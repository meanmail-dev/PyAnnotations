package ru.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression

class ReplaceRedundantOptionalQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Remove redundant Optional"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val outerOptional = descriptor.psiElement as? PySubscriptionExpression ?: return
        val innerOptional = outerOptional.children[1] as? PySubscriptionExpression ?: return
        val innerType = innerOptional.children[1]

        val elementGenerator = PyElementGenerator.getInstance(project)
        val replacementText = "Optional[${innerType.text}]"
        outerOptional.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, replacementText))
    }
}