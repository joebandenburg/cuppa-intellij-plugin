package org.forgerock.cuppa.intellij.configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.ExternalizablePath;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.execution.util.ProgramParametersUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Each instance of this class represents a single run/debug configuration for Cuppa.
 */
public class CuppaConfiguration extends JavaTestConfigurationBase {
    private static final String VM_PARAMETERS = "vm_parameters";
    private static final String PROGRAM_PARAMETERS = "program_parameters";
    private static final String WORKING_DIRECTORY = "working_directory";
    private static final String IS_ALTERNATIVE_JRE_PATH_ENABLED = "is_alternative_jre_path_enabled";
    private static final String ALTERNATIVE_JRE_PATH = "alternative_jre_path";

    private String vmParameters;
    private String programParameters;
    private String workingDirectory;
    private boolean isAlternativeJrePathEnabled = false;
    private String alternativeJrePath;
    private Map<String, String> environmentVariables = new LinkedHashMap<>();

    public CuppaConfiguration(String name, @NotNull Project project, @NotNull ConfigurationFactory factory) {
        super(name, new JavaRunConfigurationModule(project, false), factory);
    }

    @NotNull
    @Override
    public String getFrameworkPrefix() {
        return "c";
    }

    @Override
    public void setVMParameters(String value) {
        vmParameters = value;
    }

    @Override
    public String getVMParameters() {
        return vmParameters;
    }

    @Override
    public boolean isAlternativeJrePathEnabled() {
        return isAlternativeJrePathEnabled;
    }

    @Override
    public void setAlternativeJrePathEnabled(boolean enabled) {
        isAlternativeJrePathEnabled = enabled;
    }

    @Nullable
    @Override
    public String getAlternativeJrePath() {
        return alternativeJrePath;
    }

    @Override
    public void setAlternativeJrePath(String path) {
        alternativeJrePath = path;
    }

    @Nullable
    @Override
    public String getRunClass() {
        return null;
    }

    @Nullable
    @Override
    public String getPackage() {
        return null;
    }

    @Override
    public void setProgramParameters(@Nullable String value) {
        programParameters = value;
    }

    @Nullable
    @Override
    public String getProgramParameters() {
        return programParameters;
    }

    @Override
    public void setWorkingDirectory(@Nullable String value) {
        workingDirectory = ExternalizablePath.urlValue(value);
    }

    @Nullable
    @Override
    public String getWorkingDirectory() {
        return ExternalizablePath.localPathValue(workingDirectory);
    }

    @Override
    public void setEnvs(@NotNull Map<String, String> envs) {
        this.environmentVariables = envs;
    }

    @NotNull
    @Override
    public Map<String, String> getEnvs() {
        return environmentVariables;
    }

    @Override
    public void setPassParentEnvs(boolean passParentEnvs) {

    }

    @Override
    public boolean isPassParentEnvs() {
        return false;
    }

    @Override
    public Collection<Module> getValidModules() {
        return getAllModules();
    }

    @Nullable
    @Override
    public RefactoringElementListener getRefactoringElementListener(PsiElement element) {
        return null;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this);
        ProgramParametersUtil.checkWorkingDirectoryExist(this, getProject(), getConfigurationModule().getModule());
        JavaParametersUtil.checkAlternativeJRE(this);
        getConfigurationModule().checkForWarning();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<CuppaConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"),
                new CuppaConfigurationEditor<>(getProject()));
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return new CuppaRunState(this, environment);
    }

    @Override
    public SMTRunnerConsoleProperties createTestConsoleProperties(Executor executor) {
        return null;
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        writeModule(element);
        JavaRunConfigurationExtensionManager.getInstance().writeExternal(this, element);
        EnvironmentVariablesComponent.writeExternal(element, environmentVariables);
        if (StringUtil.isNotEmpty(vmParameters)) {
            JDOMExternalizerUtil.addElementWithValueAttribute(element, VM_PARAMETERS, vmParameters);
        }
        if (StringUtil.isNotEmpty(programParameters)) {
            JDOMExternalizerUtil.addElementWithValueAttribute(element, PROGRAM_PARAMETERS, programParameters);
        }
        if (StringUtil.isNotEmpty(workingDirectory)) {
            JDOMExternalizerUtil.addElementWithValueAttribute(element, WORKING_DIRECTORY, workingDirectory);
        }
        if (isAlternativeJrePathEnabled) {
            JDOMExternalizerUtil.addElementWithValueAttribute(element, IS_ALTERNATIVE_JRE_PATH_ENABLED, "true");
        }
        if (StringUtil.isNotEmpty(alternativeJrePath)) {
            JDOMExternalizerUtil.addElementWithValueAttribute(element, ALTERNATIVE_JRE_PATH, alternativeJrePath);
        }
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        readModule(element);
        JavaRunConfigurationExtensionManager.getInstance().readExternal(this, element);
        EnvironmentVariablesComponent.readExternal(element, environmentVariables);
        vmParameters = JDOMExternalizerUtil.getFirstChildValueAttribute(element, VM_PARAMETERS);
        programParameters = JDOMExternalizerUtil.getFirstChildValueAttribute(element, PROGRAM_PARAMETERS);
        workingDirectory = JDOMExternalizerUtil.getFirstChildValueAttribute(element, WORKING_DIRECTORY);
        isAlternativeJrePathEnabled = Boolean.parseBoolean(
                JDOMExternalizerUtil.getFirstChildValueAttribute(element, IS_ALTERNATIVE_JRE_PATH_ENABLED));
        alternativeJrePath = JDOMExternalizerUtil.getFirstChildValueAttribute(element, ALTERNATIVE_JRE_PATH);
    }
}