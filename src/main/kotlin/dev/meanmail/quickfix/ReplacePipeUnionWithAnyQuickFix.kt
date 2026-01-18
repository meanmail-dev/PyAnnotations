package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyElementGenerator

class ReplacePipeUnionWithAnyQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Replace with 'Any'"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PyBinaryExpression
        if (union == null || !union.isValid) {
            LOG.warn("Invalid PSI element for pipe union replacement")
            return
        }

        val elementGenerator = PyElementGenerator.getInstance(project)
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON310, "Any"))
    }

    companion object {
        private val LOG = Logger.getInstance(ReplacePipeUnionWithAnyQuickFix::class.java)
    }
}
