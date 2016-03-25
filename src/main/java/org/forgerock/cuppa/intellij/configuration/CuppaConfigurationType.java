package org.forgerock.cuppa.intellij.configuration;

import javax.swing.*;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import org.forgerock.cuppa.intellij.CuppaIcons;
import org.jetbrains.annotations.NotNull;

/**
 * An extension for displaying a run/debug configuration for Cuppa in the run/debug configurations dialog.
 */
public class CuppaConfigurationType implements ConfigurationType {
    @Override
    public String getDisplayName() {
        return "Cuppa";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "Cuppa Configuration";
    }

    @Override
    public Icon getIcon() {
        return CuppaIcons.CUPPA;
    }

    @NotNull
    @Override
    public String getId() {
        return "Cuppa";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[0];
    }
}
