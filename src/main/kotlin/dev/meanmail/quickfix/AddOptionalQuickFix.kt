package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyAnnotation
import com.jetbrains.python.psi.PyElementGenerator

class AddOptionalQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Wrap type in Optional"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val annotation = descriptor.psiElement as? PyAnnotation
        if (annotation == null || !annotation.isValid) {
            LOG.warn("Invalid PSI element for adding Optional")
            return
        }

        val annotationValue = annotation.value
        if (annotationValue == null) {
            LOG.warn("Missing annotation value")
            return
        }

        val currentType = annotationValue.text
        val newAnnotation = "Optional[$currentType]"

        val elementGenerator = PyElementGenerator.getInstance(project)
        val newAnnotationElement = elementGenerator.createExpressionFromText(
            LanguageLevel.PYTHON37,
            newAnnotation
        )

        annotationValue.replace(newAnnotationElement)
    }

    companion object {
        private val LOG = Logger.getInstance(AddOptionalQuickFix::class.java)
    }
}
