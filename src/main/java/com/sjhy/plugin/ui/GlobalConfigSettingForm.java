package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.GlobalConfig;
import com.sjhy.plugin.entity.GlobalConfigGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.ui.component.EditListComponent;
import com.sjhy.plugin.ui.component.EditorComponent;
import com.sjhy.plugin.ui.component.GroupNameComponent;
import com.sjhy.plugin.ui.component.LeftRightComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/10 16:14
 */
public class GlobalConfigSettingForm implements Configurable, BaseSettings {
    /**
     * Global variable description information, documentation
     */
    private static final String TEMPLATE_DESCRIPTION_INFO;

    static {
        String descriptionInfo = "";
        try {
            descriptionInfo = UrlUtil.loadText(GlobalConfigSettingForm.class.getResource("/description/globalConfigDescription.html"));
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
        } finally {
            TEMPLATE_DESCRIPTION_INFO = descriptionInfo;
        }
    }

    private JPanel mainPanel;
    /**
     * Type map configuration
     */
    private Map<String, GlobalConfigGroup> globalConfigGroupMap;
    /**
     * Current group name
     */
    private GlobalConfigGroup currGlobalConfigGroup;
    /**
     * Edit box component
     */
    private EditorComponent<GlobalConfig> editorComponent;
    /**
     * Group Action Components
     */
    private GroupNameComponent<GlobalConfig, GlobalConfigGroup> groupNameComponent;
    /**
     * Edit list box
     */
    private EditListComponent<GlobalConfig> editListComponent;


    public GlobalConfigSettingForm() {
        this.mainPanel = new JPanel(new BorderLayout());
    }


    private void initGroupName() {
        Consumer<GlobalConfigGroup> switchGroupOperator = globalConfigGroup -> {
            this.currGlobalConfigGroup = globalConfigGroup;
            refreshUiVal();
            // Toggle grouping edit box
            this.editorComponent.setFile(null);
        };

        this.groupNameComponent = new GroupNameComponent<>(switchGroupOperator, this.globalConfigGroupMap);
        this.mainPanel.add(groupNameComponent.getPanel(), BorderLayout.NORTH);
    }

    private void initEditList() {
        Consumer<GlobalConfig> switchItemFun = globalConfig -> {
            refreshUiVal();
            if (globalConfig != null) {
                this.editListComponent.setCurrentItem(globalConfig.getName());
            }
            editorComponent.setFile(globalConfig);
        };
        this.editListComponent = new EditListComponent<>(switchItemFun, "GlobalConfig Name:", GlobalConfig.class, this.currGlobalConfigGroup.getElementList());
    }

    private void initEditor() {
        this.editorComponent = new EditorComponent<>(null, TEMPLATE_DESCRIPTION_INFO);
    }

    private void initPanel() {
        this.loadSettingsStore(getSettingsStorage());
        // Initialize the form
        this.initGroupName();
        // Initialize the edit list component
        this.initEditList();
        // Initialize the edit box component
        this.initEditor();
        // Left and right components
        LeftRightComponent leftRightComponent = new LeftRightComponent(editListComponent.getMainPanel(), this.editorComponent.getMainPanel());
        this.mainPanel.add(leftRightComponent.getMainPanel(), BorderLayout.CENTER);
    }

    @Override
    public String getDisplayName() {
        return "Global Config";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        // Copy configuration to prevent tampering
        this.globalConfigGroupMap = CloneUtils.cloneByJson(settingsStorage.getGlobalConfigGroupMap(), new TypeReference<Map<String, GlobalConfigGroup>>() {
        });
        this.currGlobalConfigGroup = this.globalConfigGroupMap.get(settingsStorage.getCurrGlobalConfigGroupName());
        if (this.currGlobalConfigGroup == null) {
            this.currGlobalConfigGroup = this.globalConfigGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
        }
        // Solve the bug that the edit box is not cleared after reset
        if (this.editorComponent != null) {
            this.editorComponent.setFile(null);
        }
        this.refreshUiVal();
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.initPanel();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !this.globalConfigGroupMap.equals(getSettingsStorage().getGlobalConfigGroupMap())
                || !getSettingsStorage().getCurrGlobalConfigGroupName().equals(this.currGlobalConfigGroup.getName());
    }

    @Override
    public void apply() {
        getSettingsStorage().setGlobalConfigGroupMap(this.globalConfigGroupMap);
        getSettingsStorage().setCurrGlobalConfigGroupName(this.currGlobalConfigGroup.getName());
        // Reload configuration after saving package
        this.loadSettingsStore(getSettingsStorage());
    }

    private void refreshUiVal() {
        if (this.groupNameComponent != null) {
            this.groupNameComponent.setGroupMap(this.globalConfigGroupMap);
            this.groupNameComponent.setCurrGroupName(this.currGlobalConfigGroup.getName());
        }
        if (this.editListComponent != null) {
            this.editListComponent.setElementList(this.currGlobalConfigGroup.getElementList());
        }
    }
}
