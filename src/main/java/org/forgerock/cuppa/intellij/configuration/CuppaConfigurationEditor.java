package org.forgerock.cuppa.intellij.configuration;

import javax.swing.*;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.execution.ui.JrePathEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.PanelWithAnchor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CuppaConfigurationEditor<T extends CuppaConfiguration> extends SettingsEditor<T>
        implements PanelWithAnchor {
    private JPanel panel;
    private CommonJavaParametersPanel commonJavaParameters;
    private JrePathEditor jrePathEditor;
    private LabeledComponent<ModulesComboBox> moduleClasspath;

    private final ConfigurationModuleSelector moduleSelector;

    public CuppaConfigurationEditor(@NotNull Project project) {
        moduleSelector = new ConfigurationModuleSelector(project, getModulesComponent());
        jrePathEditor.setDefaultJreSelector(DefaultJreSelector.fromModuleDependencies(getModulesComponent(), false));
        commonJavaParameters.setModuleContext(moduleSelector.getModule());
        getModulesComponent().addActionListener(e -> {
            commonJavaParameters.setModuleContext(moduleSelector.getModule());
        });
        commonJavaParameters.setHasModuleMacro();

        jrePathEditor.setAnchor(moduleClasspath.getLabel());
        commonJavaParameters.setAnchor(moduleClasspath.getLabel());
    }

    @Override
    protected void resetEditorFrom(CuppaConfiguration configuration) {
        commonJavaParameters.reset(configuration);
        moduleSelector.reset(configuration);
        jrePathEditor.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
    }

    @Override
    protected void applyEditorTo(CuppaConfiguration configuration) throws ConfigurationException {
        commonJavaParameters.applyTo(configuration);
        moduleSelector.applyTo(configuration);
        configuration.setAlternativeJrePath(jrePathEditor.getJrePathOrName());
        configuration.setAlternativeJrePathEnabled(jrePathEditor.isAlternativeJreSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }

    @Override
    public JComponent getAnchor() {
        return null;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {

    }

    private ModulesComboBox getModulesComponent() {
        return moduleClasspath.getComponent();
    }
}
