package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyTargetExpression

class ConvertToTypeStatementQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Convert to type statement"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val assignment = descriptor.psiElement as? PyAssignmentStatement
        if (assignment == null || !assignment.isValid) {
            LOG.warn("Invalid PSI element for type statement conversion")
            return
        }

        val target = assignment.targets.firstOrNull() as? PyTargetExpression
        if (target == null) {
            LOG.warn("Missing target in assignment")
            return
        }

        val aliasName = target.name
        if (aliasName == null) {
            LOG.warn("Missing alias name")
            return
        }

        val assignedValue = assignment.assignedValue
        if (assignedValue == null) {
            LOG.warn("Missing assigned value")
            return
        }

        val newStatement = "type $aliasName = ${assignedValue.text}"

        val elementGenerator = PyElementGenerator.getInstance(project)
        val typeStatement = elementGenerator.createFromText(
            LanguageLevel.PYTHON312,
            com.jetbrains.python.psi.PyStatement::class.java,
            newStatement
        )

        assignment.replace(typeStatement)
    }

    companion object {
        private val LOG = Logger.getInstance(ConvertToTypeStatementQuickFix::class.java)
    }
}
