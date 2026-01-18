package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTupleExpression

class RemoveDuplicateTypesQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Remove duplicate types from Union"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PySubscriptionExpression
        if (union == null || !union.isValid) {
            LOG.warn("Invalid PSI element for duplicate removal")
            return
        }

        val indexExpression = union.children.getOrNull(1) as? PyTupleExpression
        if (indexExpression == null) {
            LOG.warn("Missing tuple expression in Union")
            return
        }

        val uniqueTypes = indexExpression.elements
            .map { it.text }
            .distinct()

        if (uniqueTypes.isEmpty()) {
            LOG.warn("No types found in Union")
            return
        }

        val newUnion = if (uniqueTypes.size == 1) {
            uniqueTypes[0]
        } else {
            "Union[${uniqueTypes.joinToString(", ")}]"
        }

        val elementGenerator = PyElementGenerator.getInstance(project)
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, newUnion))
    }

    companion object {
        private val LOG = Logger.getInstance(RemoveDuplicateTypesQuickFix::class.java)
    }
}
