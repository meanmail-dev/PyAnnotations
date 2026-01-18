package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTupleExpression

class RemoveRedundantUnionTypesQuickFix(
    private val redundantTypes: Set<String>
) : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Remove redundant subtypes from Union"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PySubscriptionExpression
        if (union == null || !union.isValid) {
            LOG.warn("Invalid PSI element for redundant type removal")
            return
        }

        val indexExpression = union.children.getOrNull(1) as? PyTupleExpression
        if (indexExpression == null) {
            LOG.warn("Missing tuple expression in Union")
            return
        }

        val remainingTypes = indexExpression.elements
            .map { it.text }
            .filter { it !in redundantTypes }

        if (remainingTypes.isEmpty()) {
            LOG.warn("No types remaining after removal")
            return
        }

        val newExpression = if (remainingTypes.size == 1) {
            remainingTypes[0]
        } else {
            "Union[${remainingTypes.joinToString(", ")}]"
        }

        val elementGenerator = PyElementGenerator.getInstance(project)
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, newExpression))
    }

    companion object {
        private val LOG = Logger.getInstance(RemoveRedundantUnionTypesQuickFix::class.java)
    }
}
