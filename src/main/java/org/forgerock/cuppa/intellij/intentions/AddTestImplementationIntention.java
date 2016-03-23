/*
 * Copyright 2016 ForgeRock AS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.forgerock.cuppa.intellij.intentions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ipp.base.Intention;
import com.siyeh.ipp.base.PsiElementPredicate;
import org.forgerock.cuppa.intellij.CuppaUtils;
import org.jetbrains.annotations.NotNull;

public final class AddTestImplementationIntention extends Intention {

    // TODO: This override shouldn't be necessary. Get this working properly with bundles.
    @NotNull
    @Override
    public String getText() {
        return "Add empty implementation";
    }

    @Override
    protected void processIntention(@NotNull PsiElement element) {
    }

    @Override
    protected void processIntention(Editor editor, @NotNull PsiElement element) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(element,
                    PsiMethodCallExpression.class);
            PsiElementFactory factory = JavaPsiFacade.getElementFactory(element.getProject());
            PsiExpressionList arguments = methodCallExpression.getArgumentList();
            PsiElement newArgument = factory.createExpressionFromText("()->{\n\n}", arguments);
            newArgument = arguments.add(newArgument);
            int line = editor.getDocument().getLineNumber(newArgument.getTextOffset());
            CaretModel caretModel = editor.getCaretModel();
            caretModel.moveToLogicalPosition(new LogicalPosition(line + 1, 0));
            PsiDocumentManager.getInstance(element.getProject()).commitDocument(editor.getDocument());
        });
        ApplicationManager.getApplication().runWriteAction(() -> {
            CaretModel caretModel = editor.getCaretModel();
            int newOffset = CodeStyleManager.getInstance(element.getProject()).adjustLineIndent(editor.getDocument(),
                    caretModel.getOffset());
            caretModel.moveToOffset(newOffset);
        });
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @NotNull
    @Override
    protected PsiElementPredicate getElementPredicate() {
        return element -> {
            if (!CuppaUtils.isCuppaClass(element)) {
                return false;
            }
            PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(element,
                    PsiMethodCallExpression.class);
            if (methodCallExpression == null) {
                return false;
            }
            JavaPsiFacade facade = JavaPsiFacade.getInstance(element.getProject());
            PsiPackage cuppaPackage = facade.findPackage("org.forgerock.cuppa");
            PsiMethod resolvedMethod = methodCallExpression.resolveMethod();
            if (resolvedMethod == null) {
                return false;
            }
            if (!resolvedMethod.getName().equals("it")) {
                return false;
            }
            PsiClass resolvedClass = resolvedMethod.getContainingClass();
            if (resolvedClass == null) {
                return false;
            }
            if (!resolvedClass.getName().equals("Cuppa")) {
                return false;
            }
            if (!facade.isInPackage(resolvedClass, cuppaPackage)) {
                return false;
            }
            if (methodCallExpression.getArgumentList().getExpressions().length != 1) {
                return false;
            }
            return true;
        };
    }
}
