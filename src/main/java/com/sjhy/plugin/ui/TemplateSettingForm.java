package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.ui.component.*;
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
public class TemplateSettingForm implements Configurable, BaseSettings {
    /**
     * Template description information, documentation
     */
    private static final String TEMPLATE_DESCRIPTION_INFO;

    static {
        String descriptionInfo = "";
        try {
            descriptionInfo = UrlUtil.loadText(TemplateSettingForm.class.getResource("/description/templateDescription.html"));
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
    private Map<String, TemplateGroup> templateGroupMap;
    /**
     * Current group name
     */
    private TemplateGroup currTemplateGroup;
    /**
     * Edit box component
     */
    private EditorComponent<Template> editorComponent;
    /**
     * Group Action Components
     */
    private GroupNameComponent<Template, TemplateGroup> groupNameComponent;
    /**
     * Edit list box
     */
    private EditListComponent<Template> editListComponent;


    public TemplateSettingForm() {
        this.mainPanel = new JPanel(new BorderLayout());
    }


    private void initGroupName() {
        Consumer<TemplateGroup> switchGroupOperator = templateGroup -> {
            this.currTemplateGroup = templateGroup;
            refreshUiVal();
            // Toggle grouping edit box
            this.editorComponent.setFile(null);
        };

        this.groupNameComponent = new GroupNameComponent<>(switchGroupOperator, this.templateGroupMap);
        this.mainPanel.add(groupNameComponent.getPanel(), BorderLayout.NORTH);
    }

    private void initEditList() {
        Consumer<Template> switchItemFun = template -> {
            refreshUiVal();
            if (template != null) {
                this.editListComponent.setCurrentItem(template.getName());
            }
            editorComponent.setFile(template);
        };
        this.editListComponent = new EditListComponent<>(switchItemFun, "Template Name:", Template.class, this.currTemplateGroup.getElementList());
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
        // Initialize live debugging
        this.initRealtimeDebug();
        // Left and right components
        LeftRightComponent leftRightComponent = new LeftRightComponent(editListComponent.getMainPanel(), this.editorComponent.getMainPanel());
        this.mainPanel.add(leftRightComponent.getMainPanel(), BorderLayout.CENTER);
    }

    private void initRealtimeDebug() {
        RealtimeDebugComponent realtimeDebugComponent = new RealtimeDebugComponent(editorComponent);
        groupNameComponent.getPanel().add(realtimeDebugComponent.getMainPanel());
    }

    @Override
    public String getDisplayName() {
        return "Template";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        // Copy configuration to prevent tampering
        this.templateGroupMap = CloneUtils.cloneByJson(settingsStorage.getTemplateGroupMap(), new TypeReference<Map<String, TemplateGroup>>() {
        });
        this.currTemplateGroup = this.templateGroupMap.get(settingsStorage.getCurrTemplateGroupName());
        if (this.currTemplateGroup == null) {
            this.currTemplateGroup = this.templateGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
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
        return !this.templateGroupMap.equals(getSettingsStorage().getTemplateGroupMap())
                || !getSettingsStorage().getCurrTemplateGroupName().equals(this.currTemplateGroup.getName());
    }

    @Override
    public void apply() {
        getSettingsStorage().setTemplateGroupMap(this.templateGroupMap);
        getSettingsStorage().setCurrTemplateGroupName(this.currTemplateGroup.getName());
        // Reload configuration after saving package
        this.loadSettingsStore(getSettingsStorage());
    }

    private void refreshUiVal() {
        if (this.groupNameComponent != null) {
            this.groupNameComponent.setGroupMap(this.templateGroupMap);
            this.groupNameComponent.setCurrGroupName(this.currTemplateGroup.getName());
        }
        if (this.editListComponent != null) {
            this.editListComponent.setElementList(this.currTemplateGroup.getElementList());
        }
    }
}
