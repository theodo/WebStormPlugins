package theodo.js.plugins.inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ecmascript6.psi.ES6ComputedName;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public class MissingSCSSClassInspection extends JSInspection {
    @NotNull
    @Override
    protected PsiElementVisitor createVisitor(@NotNull ProblemsHolder problemsHolder,
                                              @NotNull LocalInspectionToolSession localInspectionToolSession) {
        return new XmlElementVisitor() {
            @Override
            public void visitXmlAttribute(XmlAttribute xmlAttribute) {
                String name = xmlAttribute.getName();
                if ("className".equals(name)) {
                    XmlAttributeValue valueElement = xmlAttribute.getValueElement();
                    if (valueElement != null) {
                        PsiElement firstChild = valueElement.getFirstChild();
                        if (firstChild instanceof JSEmbeddedContent) {
                            JSEmbeddedContent embeddedContent = (JSEmbeddedContent) firstChild;
                            searchForReferences(embeddedContent, problemsHolder);
                        }
                    }
                }
            }
        };
    }

    private void searchForReferences(JSElement content, ProblemsHolder problemsHolder) {
        content.acceptChildren(new JSElementVisitor() {
            @Override
            public void visitJSEmbeddedContent(JSEmbeddedContent embeddedContent) {
                searchForReferences(embeddedContent, problemsHolder);
            }

            @Override
            public void visitJSCallExpression(JSCallExpression call) {
                JSArgumentList argumentList = call.getArgumentList();
                if(argumentList != null) {
                    searchForReferences(argumentList, problemsHolder);
                }
            }

            @Override
            public void visitJSReferenceExpression(JSReferenceExpression referenceExpression) {
                PsiReference reference = referenceExpression.getReference();
                if(reference != null){
                    Collection resolves = reference.resolveReference();
                    if(resolves.isEmpty()){
                        problemsHolder.registerProblem(referenceExpression, "Can't find reference of css class");
                    }
                }
            }

            @Override
            public void visitJSObjectLiteralExpression(JSObjectLiteralExpression objectLiteralExpression) {
                JSProperty[] properties = objectLiteralExpression.getProperties();
                for (JSProperty property : properties) {
                    property.acceptChildren(new JSElementVisitor(){
                        @Override
                        public void visitES6ComputedName(ES6ComputedName node) {
                            searchForReferences(node, problemsHolder);
                        }
                    });
                }
            }
        });
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String getDisplayName() {
        return "Search for usage of SCSS classnames that are not found in associated SCSS files";
    }

}