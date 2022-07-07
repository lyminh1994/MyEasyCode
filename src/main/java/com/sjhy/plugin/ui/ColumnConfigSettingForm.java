package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.entity.ColumnConfigGroup;
import com.sjhy.plugin.enums.ColumnConfigType;
import com.sjhy.plugin.factory.CellEditorFactory;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.ui.component.GroupNameComponent;
import com.sjhy.plugin.ui.component.TableComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/10 13:27
 */
public class ColumnConfigSettingForm implements Configurable, BaseSettings {
    private JPanel mainPanel;
    /**
     * Column configuration
     */
    private Map<String, ColumnConfigGroup> columnConfigGroupMap;
    /**
     * Current group name
     */
    private ColumnConfigGroup currColumnConfigGroup;
    /**
     * Form component
     */
    private TableComponent<ColumnConfig> tableComponent;
    /**
     * Group Action Components
     */
    private GroupNameComponent<ColumnConfig, ColumnConfigGroup> groupNameComponent;

    public ColumnConfigSettingForm() {
        this.mainPanel = new JPanel(new BorderLayout());
    }

    private void initTable() {
        // First column, type
        TableCellEditor typeEditor = CellEditorFactory.createComboBoxEditor(false, ColumnConfigType.class);
        TableComponent.Column<ColumnConfig> typeColumn = new TableComponent.Column<>("type", item -> item.getType().name(), (entity, val) -> entity.setType(ColumnConfigType.valueOf(val)), typeEditor);
        // Second column header
        TableCellEditor titleEditor = CellEditorFactory.createTextFieldEditor();
        TableComponent.Column<ColumnConfig> titleColumn = new TableComponent.Column<>("title", ColumnConfig::getTitle, ColumnConfig::setTitle, titleEditor);
        // Third column options
        TableCellEditor selectValueEditor = CellEditorFactory.createTextFieldEditor();
        TableComponent.Column<ColumnConfig> selectValueColumn = new TableComponent.Column<>("selectValue", ColumnConfig::getSelectValue, ColumnConfig::setSelectValue, selectValueEditor);
        List<TableComponent.Column<ColumnConfig>> columns = Arrays.asList(typeColumn, titleColumn, selectValueColumn);

        // Form initialization
        this.tableComponent = new TableComponent<>(columns, this.currColumnConfigGroup.getElementList(), ColumnConfig.class);
        this.mainPanel.add(this.tableComponent.createPanel(), BorderLayout.CENTER);
    }

    private void initGroupName() {

        // Toggle group operation
        Consumer<ColumnConfigGroup> switchGroupOperator = typeColumnConfigGroupMap -> {
            this.currColumnConfigGroup = typeColumnConfigGroupMap;
            refreshUiVal();
        };

        this.groupNameComponent = new GroupNameComponent<>(switchGroupOperator, this.columnConfigGroupMap);
        this.mainPanel.add(groupNameComponent.getPanel(), BorderLayout.NORTH);
    }

    private void initPanel() {
        this.loadSettingsStore(getSettingsStorage());
        // Initialize the form
        this.initTable();
        this.initGroupName();
    }

    @Override
    public String getDisplayName() {
        return "Column Config";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.initPanel();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !this.columnConfigGroupMap.equals(getSettingsStorage().getColumnConfigGroupMap())
                || !getSettingsStorage().getCurrColumnConfigGroupName().equals(this.currColumnConfigGroup.getName());
    }

    @Override
    public void apply() {
        getSettingsStorage().setColumnConfigGroupMap(this.columnConfigGroupMap);
        getSettingsStorage().setCurrColumnConfigGroupName(this.currColumnConfigGroup.getName());
        // Reload configuration after saving package
        this.loadSettingsStore(getSettingsStorage());
    }

    /**
     * Load configuration information
     *
     * @param settingsStorage Configuration information
     */
    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        // Copy configuration to prevent tampering
        this.columnConfigGroupMap = CloneUtils.cloneByJson(settingsStorage.getColumnConfigGroupMap(), new TypeReference<Map<String, ColumnConfigGroup>>() {
        });
        this.currColumnConfigGroup = this.columnConfigGroupMap.get(settingsStorage.getCurrColumnConfigGroupName());
        if (this.currColumnConfigGroup == null) {
            this.currColumnConfigGroup = this.columnConfigGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
        }
        this.refreshUiVal();
    }

    private void refreshUiVal() {
        if (this.tableComponent != null) {
            this.tableComponent.setDataList(this.currColumnConfigGroup.getElementList());
        }
        if (this.groupNameComponent != null) {
            this.groupNameComponent.setGroupMap(this.columnConfigGroupMap);
            this.groupNameComponent.setCurrGroupName(this.currColumnConfigGroup.getName());
        }
    }
}
