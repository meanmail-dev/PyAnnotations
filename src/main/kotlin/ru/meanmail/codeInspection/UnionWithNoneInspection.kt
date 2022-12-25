package ru.meanmail.codeInspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyTupleExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.annotations.Nls
import ru.meanmail.quickfix.ReplaceUnionWithNoneToOptionalUnionQuickFix

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
                    registerProblem(
                        node, "Replace ${node.text} to Optional[${node.text}]",
                        ReplaceUnionWithNoneToOptionalUnionQuickFix()
                    )
                } else {
                    visitElement(node)
                }
            }
        }
    }
}
