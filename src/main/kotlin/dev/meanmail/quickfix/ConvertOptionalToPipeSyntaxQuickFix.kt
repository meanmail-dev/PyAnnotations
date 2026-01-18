package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression

class ConvertOptionalToPipeSyntaxQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Convert to pipe syntax (Python 3.10+)"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val optional = descriptor.psiElement as? PySubscriptionExpression
        if (optional == null || !optional.isValid) {
            LOG.warn("Invalid PSI element for Optional conversion")
            return
        }

        val indexExpression = optional.children.getOrNull(1)
        if (indexExpression == null || !indexExpression.isValid) {
            LOG.warn("Missing index expression in Optional")
            return
        }

        val innerType = indexExpression.text
        val pipeSyntax = "$innerType | None"

        val elementGenerator = PyElementGenerator.getInstance(project)
        optional.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON310, pipeSyntax))
    }

    companion object {
        private val LOG = Logger.getInstance(ConvertOptionalToPipeSyntaxQuickFix::class.java)
    }
}
