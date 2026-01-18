package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression

class SimplifyCallableQuickFix(
    private val replacement: String
) : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Simplify Callable"
    }

    override fun getName(): String {
        return "Replace with '$replacement'"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val subscription = descriptor.psiElement as? PySubscriptionExpression
        if (subscription == null || !subscription.isValid) {
            LOG.warn("Invalid PSI element for Callable simplification")
            return
        }

        val elementGenerator = PyElementGenerator.getInstance(project)
        val newExpression = elementGenerator.createExpressionFromText(
            LanguageLevel.PYTHON37,
            replacement
        )

        subscription.replace(newExpression)
    }

    companion object {
        private val LOG = Logger.getInstance(SimplifyCallableQuickFix::class.java)
    }
}
