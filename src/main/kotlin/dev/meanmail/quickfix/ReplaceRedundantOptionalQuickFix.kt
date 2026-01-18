package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression

class ReplaceRedundantOptionalQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Remove redundant Optional"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val outerOptional = descriptor.psiElement as? PySubscriptionExpression
        if (outerOptional == null || !outerOptional.isValid) {
            LOG.warn("Invalid PSI element for outer Optional")
            return
        }

        val innerOptional = outerOptional.children.getOrNull(1) as? PySubscriptionExpression
        if (innerOptional == null || !innerOptional.isValid) {
            LOG.warn("Missing or invalid inner Optional")
            return
        }

        val innerType = innerOptional.children.getOrNull(1)
        if (innerType == null || !innerType.isValid) {
            LOG.warn("Missing inner type in Optional")
            return
        }

        val elementGenerator = PyElementGenerator.getInstance(project)
        val replacementText = "Optional[${innerType.text}]"
        outerOptional.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, replacementText))
    }

    companion object {
        private val LOG = Logger.getInstance(ReplaceRedundantOptionalQuickFix::class.java)
    }
}