package ru.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PySubscriptionExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.annotations.Nls
import ru.meanmail.quickfix.ReplaceRedundantOptionalQuickFix

class RedundantOptionalInspection : PyInspection() {
    @Nls
    override fun getDisplayName(): String {
        return "Redundant Optional[Optional[...]]"
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        val context = PyInspectionVisitor.getContext(session)
        return Visitor(holder, context)
    }

    companion object {
        class Visitor(
            holder: ProblemsHolder?,
            context: TypeEvalContext
        ) : BaseInspectionVisitor(holder, context) {

            override fun visitPySubscriptionExpression(node: PySubscriptionExpression) {
                if (node.firstChild.text == "Optional" && node.children.count() == 2) {
                    val secondChild = node.children[1]
                    if (secondChild is PySubscriptionExpression && secondChild.firstChild.text == "Optional") {
                        registerProblem(
                            node,
                            "Redundant Optional: ${node.text} can be simplified to Optional[${secondChild.children[1].text}]",
                            ReplaceRedundantOptionalQuickFix()
                        )
                    } else {
                        super.visitPySubscriptionExpression(node)
                    }
                } else {
                    super.visitPySubscriptionExpression(node)
                }
            }
        }
    }
}