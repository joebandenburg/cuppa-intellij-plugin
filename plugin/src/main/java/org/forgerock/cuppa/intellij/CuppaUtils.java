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

package org.forgerock.cuppa.intellij;

import java.util.List;
import java.util.stream.Collectors;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.search.searches.AnnotatedPackagesSearch;
import com.intellij.psi.search.searches.AnnotationTargetsSearch;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Utility functions for integrating with IntelliJ's APIs.
 */
public final class CuppaUtils {
    public static final String TEST_ANNOTATION_FQCN = "org.forgerock.cuppa.Test";

    private CuppaUtils() {
    }

    /**
     * Determine if the given PSI element is nested within a Java class annotated with Cuppa's Test annotation.
     *
     * @param psiElement The PSI element.
     * @return true if the given element is nested in a Cuppa test class and false otherwise.
     */
    public static boolean isCuppaClass(PsiElement psiElement) {
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class, false);
        return psiClass != null && AnnotationUtil.isAnnotated(psiClass, TEST_ANNOTATION_FQCN, true);
    }

    public static List<PsiClass> findTestClasses(Module module) {
        return AnnotatedElementsSearch.searchPsiClasses(getTestAnnotationClass(module.getProject()),
                GlobalSearchScope.moduleScope(module)).findAll().stream()
                .collect(Collectors.toList());
    }

    private static PsiClass getTestAnnotationClass(Project project) {
        return JavaPsiFacade.getInstance(project).findClass(TEST_ANNOTATION_FQCN, GlobalSearchScope.allScope(project));
    }
}
