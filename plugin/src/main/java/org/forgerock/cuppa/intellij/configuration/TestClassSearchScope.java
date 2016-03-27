package org.forgerock.cuppa.intellij.configuration;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * Represents the search scope for finding Cuppa test classes to run in a run/debug configuration.
 */
public enum TestClassSearchScope {
    PROJECT("Project"),
    MODULE("Module"),
    PACKAGE("Package"),
    CLASS("Class");

    public final String label;

    TestClassSearchScope(String label) {
        this.label = label;
    }
}
