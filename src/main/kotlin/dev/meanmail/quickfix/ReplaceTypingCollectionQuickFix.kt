package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression

class ReplaceTypingCollectionQuickFix(
    private val oldType: String,
    private val newType: String
) : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Replace deprecated typing collection"
    }

    override fun getName(): String {
        return "Replace '$oldType' with '$newType'"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val subscription = descriptor.psiElement as? PySubscriptionExpression
        if (subscription == null || !subscription.isValid) {
            LOG.warn("Invalid PSI element for typing collection replacement")
            return
        }

        val indexExpression = subscription.indexExpression
        if (indexExpression == null) {
            LOG.warn("Missing index expression in subscription")
            return
        }

        val newExpression = "$newType[${indexExpression.text}]"

        val elementGenerator = PyElementGenerator.getInstance(project)
        subscription.replace(
            elementGenerator.createExpressionFromText(LanguageLevel.PYTHON39, newExpression)
        )
    }

    companion object {
        private val LOG = Logger.getInstance(ReplaceTypingCollectionQuickFix::class.java)
    }
}
