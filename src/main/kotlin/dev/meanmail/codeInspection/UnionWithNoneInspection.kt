package dev.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyTupleExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import dev.meanmail.quickfix.ReplaceUnionWithNoneToOptionalUnionQuickFix
import org.jetbrains.annotations.Nls

class UnionWithNoneInspection : PyInspection() {
    @Nls
    override fun getDisplayName(): String {
        return "Union[..., None] instead Optional[Union[...]]"
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
        ) :
            BaseInspectionVisitor(holder, context) {
            override fun visitPyAnnotationUnionExpression(
                node: PyExpression, items: PyTupleExpression
            ) {
                if (hasChildren(items, "None|NoneType")) {
                    // Check if Union has only None
                    if (items.children.count() == 1 && items.children.all { it.text.matches("('|\"|)(None|NoneType)\\1".toRegex()) }) {
                        registerProblem(
                            node, "Simplify ${node.text} to None - Union with only None is redundant",
                            ReplaceUnionWithNoneToOptionalUnionQuickFix()
                        )
                    } else {
                        registerProblem(
                            node, "Use Optional instead of Union with None: replace ${node.text} with Optional[...]",
                            ReplaceUnionWithNoneToOptionalUnionQuickFix()
                        )
                    }
                } else {
                    visitElement(node)
                }
            }
        }
    }
}
