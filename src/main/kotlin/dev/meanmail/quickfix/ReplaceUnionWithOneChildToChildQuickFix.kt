package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression

class ReplaceUnionWithOneChildToChildQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Replace Union[item] to 'item'"
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
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, indexExpression.text))
    }

    companion object {
        private val LOG = Logger.getInstance(ReplaceUnionWithOneChildToChildQuickFix::class.java)
    }
}
