package dev.meanmail.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.PyTupleExpression

class FlattenNestedUnionQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Flatten nested Union types"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val union = descriptor.psiElement as? PySubscriptionExpression
        if (union == null || !union.isValid) {
            LOG.warn("Invalid PSI element for Union flattening")
            return
        }

        val indexExpression = union.children.getOrNull(1) as? PyTupleExpression
        if (indexExpression == null) {
            LOG.warn("Missing tuple expression in Union")
            return
        }

        val flattenedTypes = mutableListOf<String>()
        collectTypes(indexExpression, flattenedTypes)

        if (flattenedTypes.isEmpty()) {
            LOG.warn("No types found in Union")
            return
        }

        val newUnion = if (flattenedTypes.size == 1) {
            flattenedTypes[0]
        } else {
            "Union[${flattenedTypes.joinToString(", ")}]"
        }

        val elementGenerator = PyElementGenerator.getInstance(project)
        union.replace(elementGenerator.createExpressionFromText(LanguageLevel.PYTHON37, newUnion))
    }

    private fun collectTypes(tuple: PyTupleExpression, result: MutableList<String>) {
        for (element in tuple.elements) {
            val subscription = element as? PySubscriptionExpression
            if (subscription != null && subscription.firstChild?.text == "Union") {
                val nestedTuple = subscription.children.getOrNull(1) as? PyTupleExpression
                if (nestedTuple != null) {
                    collectTypes(nestedTuple, result)
                } else {
                    val singleType = subscription.children.getOrNull(1)
                    if (singleType != null) {
                        result.add(singleType.text)
                    }
                }
            } else {
                result.add(element.text)
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(FlattenNestedUnionQuickFix::class.java)
    }
}
