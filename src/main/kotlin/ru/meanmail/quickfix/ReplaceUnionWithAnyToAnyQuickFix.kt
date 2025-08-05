package ru.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression

class ReplaceUnionWithAnyToAnyQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Replace Union[..., Any] to 'Any'"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PySubscriptionExpression ?: return
        val elementGenerator = PyElementGenerator.getInstance(project)
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, "Any"))
    }
}