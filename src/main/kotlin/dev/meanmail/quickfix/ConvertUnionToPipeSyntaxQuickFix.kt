package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTupleExpression

class ConvertUnionToPipeSyntaxQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Convert to pipe union syntax (Python 3.10+)"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PySubscriptionExpression
        if (union == null || !union.isValid) {
            LOG.warn("Invalid PSI element for Union conversion")
            return
        }

        val indexExpression = union.children.getOrNull(1)
        if (indexExpression == null || !indexExpression.isValid) {
            LOG.warn("Missing index expression in Union")
            return
        }

        val types = when (indexExpression) {
            is PyTupleExpression -> indexExpression.elements.map { it.text }
            else -> listOf(indexExpression.text)
        }

        if (types.isEmpty()) {
            LOG.warn("No types found in Union")
            return
        }

        val pipeSyntax = types.joinToString(" | ")
        val elementGenerator = PyElementGenerator.getInstance(project)
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON310, pipeSyntax))
    }

    companion object {
        private val LOG = Logger.getInstance(ConvertUnionToPipeSyntaxQuickFix::class.java)
    }
}
