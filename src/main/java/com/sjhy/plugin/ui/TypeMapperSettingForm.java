package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.TypeMapper;
import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.enums.MatchType;
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
 * @since 2021/08/07 15:33
 */
public class TypeMapperSettingForm implements BaseSettings {
    private JPanel mainPanel;
    /**
     * Type map configuration
     */
    private Map<String, TypeMapperGroup> typeMapperGroupMap;
    /**
     * Current group name
     */
    private TypeMapperGroup currTypeMapperGroup;
    /**
     * Form component
     */
    private TableComponent<TypeMapper> tableComponent;
    /**
     * Group Action Components
     */
    private GroupNameComponent<TypeMapper, TypeMapperGroup> groupNameComponent;

    public TypeMapperSettingForm() {
        this.mainPanel = new JPanel(new BorderLayout());
    }

    private void initTable() {
        // The first column only works with dropdown boxes
        TableCellEditor matchTypeEditor = CellEditorFactory.createComboBoxEditor(false, MatchType.class);
        TableComponent.Column<TypeMapper> matchTypeColumn = new TableComponent.Column<>("matchType",
                item -> item.getMatchType() != null ? item.getMatchType().name() : MatchType.REGEX.name(),
                (entity, val) -> entity.setMatchType(MatchType.valueOf(val)),
                matchTypeEditor
        );
        // The second column monitors the input state and modifies the attribute value in time
        TableCellEditor columnTypeEditor = CellEditorFactory.createTextFieldEditor();
        TableComponent.Column<TypeMapper> columnTypeColumn = new TableComponent.Column<>("columnType", TypeMapper::getColumnType, TypeMapper::setColumnType, columnTypeEditor);
        // The third column supports drop-down boxes
        TableCellEditor javaTypeEditor = CellEditorFactory.createComboBoxEditor(true, GlobalDict.DEFAULT_JAVA_TYPE_LIST);
        TableComponent.Column<TypeMapper> javaTypeColumn = new TableComponent.Column<>("javaType", TypeMapper::getJavaType, TypeMapper::setJavaType, javaTypeEditor);
        List<TableComponent.Column<TypeMapper>> columns = Arrays.asList(matchTypeColumn, columnTypeColumn, javaTypeColumn);
        // Form initialization
        this.tableComponent = new TableComponent<>(columns, this.currTypeMapperGroup.getElementList(), TypeMapper.class);
        this.mainPanel.add(this.tableComponent.createPanel(), BorderLayout.CENTER);
    }

    private void initGroupName() {
        // Toggle group operation
        Consumer<TypeMapperGroup> switchGroupOperator = typeMapperGroupMap -> {
            this.currTypeMapperGroup = typeMapperGroupMap;
            refreshUiVal();
        };
        this.groupNameComponent = new GroupNameComponent<>(switchGroupOperator, this.typeMapperGroupMap);
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
        return "Type Mapper";
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.initPanel();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !this.typeMapperGroupMap.equals(getSettingsStorage().getTypeMapperGroupMap())
                || !getSettingsStorage().getCurrTypeMapperGroupName().equals(this.currTypeMapperGroup.getName());
    }

    @Override
    public void apply() {
        getSettingsStorage().setTypeMapperGroupMap(this.typeMapperGroupMap);
        getSettingsStorage().setCurrTypeMapperGroupName(this.currTypeMapperGroup.getName());
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
        this.typeMapperGroupMap = CloneUtils.cloneByJson(settingsStorage.getTypeMapperGroupMap(), new TypeReference<Map<String, TypeMapperGroup>>() {
        });
        this.currTypeMapperGroup = this.typeMapperGroupMap.get(settingsStorage.getCurrTypeMapperGroupName());
        if (this.currTypeMapperGroup == null) {
            this.currTypeMapperGroup = this.typeMapperGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
        }
        this.refreshUiVal();
    }

    private void refreshUiVal() {
        if (this.tableComponent != null) {
            this.tableComponent.setDataList(this.currTypeMapperGroup.getElementList());
        }
        if (this.groupNameComponent != null) {
            this.groupNameComponent.setGroupMap(this.typeMapperGroupMap);
            this.groupNameComponent.setCurrGroupName(this.currTypeMapperGroup.getName());
        }
    }
}
