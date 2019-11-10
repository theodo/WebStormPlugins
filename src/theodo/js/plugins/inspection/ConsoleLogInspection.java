package theodo.js.plugins.inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.roots.TestSourcesFilter.isTestSources;


public class ConsoleLogInspection extends JSInspection {
    @NotNull
    @Override
    protected PsiElementVisitor createVisitor(@NotNull ProblemsHolder problemsHolder,
                                              @NotNull LocalInspectionToolSession localInspectionToolSession) {
        PsiFile containingFile = localInspectionToolSession.getFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        Project project = containingFile.getProject();

        boolean testSources = isTestSources(virtualFile, project);
        if(testSources) return PsiElementVisitor.EMPTY_VISITOR;

        return new JSElementVisitor() {
            @Override
            public void visitJSCallExpression(JSCallExpression callExpression) {
                JSExpression methodExpression = callExpression.getMethodExpression();
                if(methodExpression instanceof JSReferenceExpression){
                    JSReferenceExpression jsReferenceExpression= (JSReferenceExpression) methodExpression;
                    JSExpression qualifier = jsReferenceExpression.getQualifier();
                    if(qualifier != null){
                        if("console".equals(qualifier.getText())){
                            problemsHolder.registerProblem(callExpression, "Usage of console methods in Production code");
                        }
                    }
                }
            }
        };
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return GroupNames.LOGGING_GROUP_NAME;
    }

    @NotNull
    public String getDisplayName() {
        return "Search for usage of console.log(...), console.warn(...), and so on...";
    }

}