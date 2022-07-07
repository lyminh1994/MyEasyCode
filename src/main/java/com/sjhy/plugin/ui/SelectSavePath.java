package com.sjhy.plugin.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.constants.StrState;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.GenerateOptions;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.service.CodeGenerateService;
import com.sjhy.plugin.service.SettingsStorageService;
import com.sjhy.plugin.service.TableInfoSettingsService;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.tool.ModuleUtils;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.tool.StringUtils;
import com.sjhy.plugin.ui.component.TemplateSelectComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Choose a save path
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class SelectSavePath extends DialogWrapper {

    /**
     * Main panel
     */
    private JPanel contentPane;
    /**
     * Model drop-down box
     */
    private JComboBox<String> moduleComboBox;
    /**
     * Packet field
     */
    private JTextField packageField;
    /**
     * Path field
     */
    private JTextField pathField;
    /**
     * Prefix field
     */
    private JTextField preField;
    /**
     * Package selection button
     */
    private JButton packageChooseButton;
    /**
     * Path selection button
     */
    private JButton pathChooseButton;
    /**
     * Template panel
     */
    private JPanel templatePanel;
    /**
     * Unified configuration checkbox
     */
    private JCheckBox unifiedConfigCheckBox;
    /**
     * Pop-up check box
     */
    private JCheckBox titleSureCheckBox;
    /**
     * Format code checkbox
     */
    private JCheckBox reFormatCheckBox;
    /**
     * No pop-up check box
     */
    private JCheckBox titleRefuseCheckBox;
    /**
     * Data caching tool class
     */
    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();
    /**
     * Table Information Services
     */
    private TableInfoSettingsService tableInfoService;
    /**
     * Project object
     */
    private Project project;
    /**
     * Code generation service
     */
    private CodeGenerateService codeGenerateService;
    /**
     * Modules in the current project
     */
    private List<Module> moduleList;

    /**
     * Entity schema generation code
     */
    private boolean entityMode;

    /**
     * Template selection component
     */
    private TemplateSelectComponent templateSelectComponent;

    /**
     * Construction method
     */
    public SelectSavePath(Project project) {
        this(project, false);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.contentPane;
    }

    /**
     * Construction method
     */
    public SelectSavePath(Project project, boolean entityMode) {
        super(project);
        this.entityMode = entityMode;
        this.project = project;
        this.tableInfoService = TableInfoSettingsService.getInstance();
        this.codeGenerateService = CodeGenerateService.getInstance(project);
        // Initialize the module, which exists at the front of the resource path
        this.moduleList = new LinkedList<>();
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            // The existing source code folder is placed in the front, otherwise it is placed in the back
            if (ModuleUtils.existsSourcePath(module)) {
                this.moduleList.add(0, module);
            } else {
                this.moduleList.add(module);
            }
        }
        this.initPanel();
        this.refreshData();
        this.initEvent();
        init();
        setTitle(GlobalDict.TITLE_INFO);
        //Initialization path
        refreshPath();
    }

    private void initEvent() {
        //Listen to the module selection event
        moduleComboBox.addActionListener(e ->
            // Refresh path
            refreshPath()
        );

        try {
            Class<?> cls = Class.forName("com.intellij.ide.util.PackageChooserDialog");
            //Add package selection event
            packageChooseButton.addActionListener(e -> {
                try {
                    Constructor<?> constructor = cls.getConstructor(String.class, Project.class);
                    Object dialog = constructor.newInstance("Choose Package", project);
                    // Display window
                    Method showMethod = cls.getMethod("show");
                    showMethod.invoke(dialog);
                    // Get the selected package name
                    Method getSelectedPackageMethod = cls.getMethod("getSelectedPackage");
                    Object psiPackage = getSelectedPackageMethod.invoke(dialog);
                    if (psiPackage != null) {
                        Method getQualifiedNameMethod = psiPackage.getClass().getMethod("getQualifiedName");
                        String packageName = (String) getQualifiedNameMethod.invoke(psiPackage);
                        packageField.setText(packageName);
                        // Refresh path
                        refreshPath();
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e1) {
                    ExceptionUtil.rethrow(e1);
                }
            });

            // Add package edit box lose focus event
            packageField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    // Refresh path
                    refreshPath();
                }
            });
        } catch (ClassNotFoundException e) {
            // No PackageChooserDialog, not an IDE that supports Java, disable related UI components
            packageField.setEnabled(false);
            packageChooseButton.setEnabled(false);
        }

        //Choose a path
        pathChooseButton.addActionListener(e -> {
            //Set the currently selected model as the base path
            VirtualFile path = ProjectUtils.getBaseDir(project);
            Module module = getSelectModule();
            if (module != null) {
                path = ModuleUtils.getSourcePath(module);
            }
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, path);
            if (virtualFile != null) {
                pathField.setText(virtualFile.getPath());
            }
        });
    }

    private void refreshData() {
        // Get the selected table information (the table with the right mouse button), and prompt for unknown type
        TableInfo tableInfo;
        if (entityMode) {
            tableInfo = tableInfoService.getTableInfo(cacheDataUtils.getSelectPsiClass());
        } else {
            tableInfo = tableInfoService.getTableInfo(cacheDataUtils.getSelectDbTable());
        }

        // Set default configuration information
        if (!StringUtils.isEmpty(tableInfo.getSaveModelName())) {
            moduleComboBox.setSelectedItem(tableInfo.getSaveModelName());
        }
        if (!StringUtils.isEmpty(tableInfo.getSavePackageName())) {
            packageField.setText(tableInfo.getSavePackageName());
        }
        if (!StringUtils.isEmpty(tableInfo.getPreName())) {
            preField.setText(tableInfo.getPreName());
        }
        SettingsStorageDTO settings = SettingsStorageService.getSettingsStorage();
        String groupName = settings.getCurrTemplateGroupName();
        if (!StringUtils.isEmpty(tableInfo.getTemplateGroupName()) && settings.getTemplateGroupMap().containsKey(tableInfo.getTemplateGroupName())) {
            groupName = tableInfo.getTemplateGroupName();

        }
        templateSelectComponent.setSelectedGroupName(groupName);
        String savePath = tableInfo.getSavePath();
        if (!StringUtils.isEmpty(savePath)) {
            // Determine if you need to splice the project path
            if (savePath.startsWith(StrState.RELATIVE_PATH)) {
                String projectPath = project.getBasePath();
                savePath = projectPath + savePath.substring(1);
            }
            pathField.setText(savePath);
        }
    }

    @Override
    protected void doOKAction() {
        onOK();
        super.doOKAction();
    }

    /**
     * Confirm button callback event
     */
    private void onOK() {
        List<Template> selectTemplateList = templateSelectComponent.getAllSelectedTemplate();
        // If the selected template is empty
        if (selectTemplateList.isEmpty()) {
            Messages.showWarningDialog("Can't select template!", GlobalDict.TITLE_INFO);
            return;
        }
        String savePath = pathField.getText();
        if (StringUtils.isEmpty(savePath)) {
            Messages.showWarningDialog("Can't select save path!", GlobalDict.TITLE_INFO);
            return;
        }
        // Do processing for Linux system paths
        savePath = savePath.replace("\\", "/");
        // Save the path using a relative path
        String basePath = project.getBasePath();
        if (!StringUtils.isEmpty(basePath) && savePath.startsWith(basePath)) {
            if (savePath.length() > basePath.length()) {
                if ("/".equals(savePath.substring(basePath.length(), basePath.length() + 1))) {
                    savePath = savePath.replace(basePath, ".");
                }
            } else {
                savePath = savePath.replace(basePath, ".");
            }
        }
        // Save configuration
        TableInfo tableInfo;
        if (!entityMode) {
            tableInfo = tableInfoService.getTableInfo(cacheDataUtils.getSelectDbTable());
        } else {
            tableInfo = tableInfoService.getTableInfo(cacheDataUtils.getSelectPsiClass());
        }
        tableInfo.setSavePath(savePath);
        tableInfo.setSavePackageName(packageField.getText());
        tableInfo.setPreName(preField.getText());
        tableInfo.setTemplateGroupName(templateSelectComponent.getSelectedGroupName());
        Module module = getSelectModule();
        if (module != null) {
            tableInfo.setSaveModelName(module.getName());
        }
        // Save configuration
        tableInfoService.saveTableInfo(tableInfo);

        // Generate code
        codeGenerateService.generate(selectTemplateList, getGenerateOptions());
    }

    /**
     * Initialization method
     */
    private void initPanel() {
        // Initialize template group
        this.templateSelectComponent = new TemplateSelectComponent();
        templatePanel.add(this.templateSelectComponent.getMainPanel(), BorderLayout.CENTER);

        //Initialize Module selection
        for (Module module : this.moduleList) {
            moduleComboBox.addItem(module.getName());
        }
    }

    /**
     * Get build options
     *
     * @return {@link GenerateOptions}
     */
    private GenerateOptions getGenerateOptions() {
        return GenerateOptions.builder()
                .entityModel(this.entityMode)
                .reFormat(reFormatCheckBox.isSelected())
                .titleSure(titleSureCheckBox.isSelected())
                .titleRefuse(titleRefuseCheckBox.isSelected())
                .unifiedConfig(unifiedConfigCheckBox.isSelected())
                .build();
    }

    /**
     * Get the selected Module
     *
     * @return Selected Module
     */
    private Module getSelectModule() {
        String name = (String) moduleComboBox.getSelectedItem();
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return ModuleManager.getInstance(project).findModuleByName(name);
    }

    /**
     * Get base path
     *
     * @return Base path
     */
    private String getBasePath() {
        Module module = getSelectModule();
        VirtualFile baseVirtualFile = ProjectUtils.getBaseDir(project);
        if (baseVirtualFile == null) {
            Messages.showWarningDialog("Unable to get the project base path!", GlobalDict.TITLE_INFO);
            return "";
        }
        String baseDir = baseVirtualFile.getPath();
        if (module != null) {
            VirtualFile virtualFile = ModuleUtils.getSourcePath(module);
            if (virtualFile != null) {
                baseDir = virtualFile.getPath();
            }
        }
        return baseDir;
    }

    /**
     * Refresh directory
     */
    private void refreshPath() {
        String packageName = packageField.getText();
        // Get base path
        String path = getBasePath();
        // Compatible Linux path
        path = path.replace("\\", "/");
        // If package path exists, add package path
        if (!StringUtils.isEmpty(packageName)) {
            path += "/" + packageName.replace(".", "/");
        }
        pathField.setText(path);
    }
}
