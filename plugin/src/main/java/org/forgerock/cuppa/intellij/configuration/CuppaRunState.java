package org.forgerock.cuppa.intellij.configuration;

import static org.forgerock.cuppa.intellij.CuppaUtils.TEST_ANNOTATION_FQCN;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestFrameworkRunnableState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestSearchScope;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotationTargetsSearch;
import com.intellij.util.PathUtil;
import org.forgerock.cuppa.intellij.CuppaUtils;
import org.forgerock.cuppa.intellij.rt.CuppaRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Starts and monitors a Cuppa run/debug configuration.
 */
public class CuppaRunState extends JavaTestFrameworkRunnableState<CuppaConfiguration> {
    private static final String CUPPA_FRAMEWORK_NAME = "Cuppa";

    private final CuppaConfiguration configuration;

    public CuppaRunState(CuppaConfiguration configuration, ExecutionEnvironment environment) {
        super(environment);
        this.configuration = configuration;
    }

    @NotNull
    @Override
    protected String getFrameworkName() {
        return CUPPA_FRAMEWORK_NAME;
    }

    @NotNull
    @Override
    protected String getFrameworkId() {
        return "cuppa";
    }

    @Override
    protected void passTempFile(ParametersList parametersList, String tempFilePath) {

    }

    @NotNull
    @Override
    protected CuppaConfiguration getConfiguration() {
        return configuration;
    }

    @Nullable
    @Override
    protected TestSearchScope getScope() {
        return null;
    }

    @NotNull
    @Override
    protected String getForkMode() {
        return "none";
    }

    @NotNull
    @Override
    protected OSProcessHandler createHandler(Executor executor) throws ExecutionException {
        appendForkInfo(executor);
        return startProcess();
    }

    @Override
    protected void configureRTClasspath(JavaParameters javaParameters) {
        javaParameters.getClassPath().add(PathUtil.getJarPathForClass(CuppaRunner.class));
    }

    @Override
    protected void passForkMode(String forkMode, File tempFile, JavaParameters parameters) throws ExecutionException {

    }

    @Override
    protected JavaParameters createJavaParameters() throws ExecutionException {
        JavaParameters parameters = super.createJavaParameters();
        Module module = configuration.getConfigurationModule().getModule();
        parameters.setMainClass(CuppaRunner.class.getName());
        List<String> testClassNames = CuppaUtils.findTestClasses(module).stream()
                .map(PsiClass::getQualifiedName)
                .collect(Collectors.toList());
        parameters.getProgramParametersList().addAll(testClassNames);
        return parameters;
    }
}
