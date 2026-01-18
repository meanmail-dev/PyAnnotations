package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PySubscriptionExpression

class RemoveRedundantGenericQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Remove redundant Generic"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val genericExpr = descriptor.psiElement as? PySubscriptionExpression
        if (genericExpr == null || !genericExpr.isValid) {
            LOG.warn("Invalid PSI element for Generic removal")
            return
        }

        val parent = genericExpr.parent
        if (parent is PyArgumentList) {
            // Find the comma before or after this element and remove it properly
            val prevSibling = genericExpr.prevSibling
            val nextSibling = genericExpr.nextSibling

            genericExpr.delete()

            // Clean up comma
            if (prevSibling?.text == ", ") {
                prevSibling.delete()
            } else if (prevSibling?.text == ",") {
                prevSibling.delete()
            } else if (nextSibling?.text == ", ") {
                nextSibling.delete()
            } else if (nextSibling?.text == ",") {
                nextSibling.delete()
            }
        } else {
            genericExpr.delete()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(RemoveRedundantGenericQuickFix::class.java)
    }
}
