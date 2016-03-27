package org.forgerock.cuppa.intellij.configuration;

import javax.swing.*;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configuration.BrowseModuleValueActionListener;
import com.intellij.execution.ui.ClassBrowser;
import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.execution.ui.JrePathEditor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.util.ui.UIUtil;
import org.forgerock.cuppa.intellij.CuppaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The UI panel to allow the user to edit the settings of a Cuppa configuration.
 *
 * @param <T>
 */
public class CuppaConfigurationEditor<T extends CuppaConfiguration> extends SettingsEditor<T>
        implements PanelWithAnchor {
    private JPanel panel;
    private JComponent anchor;
    private CommonJavaParametersPanel commonJavaParameters;
    private JrePathEditor jrePathEditor;
    private LabeledComponent<ModulesComboBox> moduleClasspath;
    private LabeledComponent<JComboBox<TestClassSearchScope>> searchScope;
    private LabeledComponent<EditorTextFieldWithBrowseButton> searchPackage;
    private LabeledComponent<EditorTextFieldWithBrowseButton> searchClass;

    private final ConfigurationModuleSelector moduleSelector;

    public CuppaConfigurationEditor(@NotNull Project project) {
        ComboBoxModel<TestClassSearchScope> searchTypeModel = new EnumComboBoxModel<>(TestClassSearchScope.class);
        searchScope.getComponent().setModel(searchTypeModel);
        searchScope.getComponent().setRenderer(new ListCellRendererWrapper<TestClassSearchScope>() {
            @Override
            public void customize(JList list, TestClassSearchScope value, int index, boolean selected, boolean hasFocus) {
                setText(value.label);
            }
        });
        searchScope.getComponent().addActionListener(e ->
                onSearchTypeChanged((TestClassSearchScope) searchScope.getComponent().getSelectedItem()));
        onSearchTypeChanged((TestClassSearchScope) searchTypeModel.getSelectedItem());

        moduleSelector = new ConfigurationModuleSelector(project, getModulesComponent());
        searchPackage.setComponent(new EditorTextFieldWithBrowseButton(project, false));
        new PackageBrowser(project).setField(searchPackage.getComponent());
        searchClass.setComponent(new EditorTextFieldWithBrowseButton(project, true));
        new TestClassBrowser(project).setField(searchClass.getComponent());

        UIUtil.setEnabled(commonJavaParameters.getProgramParametersComponent(), false, true);

        jrePathEditor.setDefaultJreSelector(DefaultJreSelector.fromModuleDependencies(getModulesComponent(), false));
        commonJavaParameters.setModuleContext(moduleSelector.getModule());
        getModulesComponent().addActionListener(e -> {
            commonJavaParameters.setModuleContext(moduleSelector.getModule());
        });
        commonJavaParameters.setHasModuleMacro();

        setAnchor(commonJavaParameters.getAnchor());
    }

    private void onSearchTypeChanged(TestClassSearchScope searchType) {
        moduleClasspath.setVisible(searchType == TestClassSearchScope.MODULE);
        searchPackage.setVisible(searchType == TestClassSearchScope.PACKAGE);
        searchClass.setVisible(searchType == TestClassSearchScope.CLASS);
    }

    @Override
    protected void resetEditorFrom(CuppaConfiguration configuration) {
        commonJavaParameters.reset(configuration);
        moduleSelector.reset(configuration);
        jrePathEditor.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
        searchScope.getComponent().getModel().setSelectedItem(configuration.getSearchScope());
        searchPackage.getComponent().setText(configuration.getSearchPackage());
        searchClass.getComponent().setText(configuration.getSearchClass());
    }

    @Override
    protected void applyEditorTo(CuppaConfiguration configuration) throws ConfigurationException {
        commonJavaParameters.applyTo(configuration);
        TestClassSearchScope scope = (TestClassSearchScope) searchScope.getComponent().getModel().getSelectedItem();
        if (scope == TestClassSearchScope.MODULE) {
            moduleSelector.applyTo(configuration);
        } else {
            configuration.setModule(null);
        }
        configuration.setAlternativeJrePath(jrePathEditor.getJrePathOrName());
        configuration.setAlternativeJrePathEnabled(jrePathEditor.isAlternativeJreSelected());
        configuration.setSearchScope(scope);
        configuration.setSearchPackage(searchPackage.getComponent().getText());
        configuration.setSearchClass(searchClass.getComponent().getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }

    @Override
    public JComponent getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        this.anchor = anchor;
        jrePathEditor.setAnchor(anchor);
        commonJavaParameters.setAnchor(anchor);
        searchScope.setAnchor(anchor);
        moduleClasspath.setAnchor(anchor);
        searchPackage.setAnchor(anchor);
        searchClass.setAnchor(anchor);
    }

    private ModulesComboBox getModulesComponent() {
        return moduleClasspath.getComponent();
    }

    private final class PackageBrowser extends BrowseModuleValueActionListener<EditorTextField> {
        public PackageBrowser(Project project) {
            super(project);
        }

        @Nullable
        @Override
        protected String showDialog() {
            PackageChooserDialog dialog =
                    new PackageChooserDialog(ExecutionBundle.message("choose.package.dialog.title"), getProject());
            dialog.show();
            PsiPackage selectedPackage = dialog.getSelectedPackage();
            return selectedPackage == null ? null : selectedPackage.getQualifiedName();
        }
    }

    private final class TestClassBrowser extends ClassBrowser {
        public TestClassBrowser(Project project) {
            super(project, ExecutionBundle.message("choose.test.class.dialog.title"));
        }

        @Override
        protected ClassFilter.ClassFilterWithScope getFilter() throws NoFilterException {
            return new TestClassFilter(GlobalSearchScope.projectScope(getProject()));
        }

        @Override
        protected PsiClass findClass(String className) {
            return moduleSelector.findClass(className);
        }
    }

    private final class TestClassFilter implements ClassFilter.ClassFilterWithScope {
        private final GlobalSearchScope scope;

        private TestClassFilter(GlobalSearchScope scope) {
            this.scope = scope;
        }

        @Override
        public GlobalSearchScope getScope() {
            return scope;
        }

        @Override
        public boolean isAccepted(PsiClass aClass) {
            return CuppaUtils.isCuppaClass(aClass);
        }
    }
}
