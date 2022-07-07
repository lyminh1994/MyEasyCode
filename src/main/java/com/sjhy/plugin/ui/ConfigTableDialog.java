package com.sjhy.plugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.factory.CellEditorFactory;
import com.sjhy.plugin.service.TableInfoSettingsService;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.tool.CurrGroupUtils;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.tool.StringUtils;
import com.sjhy.plugin.ui.base.ConfigTableModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;

/**
 * Table configuration window
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class ConfigTableDialog extends DialogWrapper {
    /**
     * Main panel
     */
    private JPanel mainPanel;
    /**
     * Table info object
     */
    private TableInfo tableInfo;

    public ConfigTableDialog() {
        super(ProjectUtils.getCurrProject());
        this.mainPanel = new JPanel(new BorderLayout());
        this.initPanel();
    }

    private void initPanel() {
        init();
        this.tableInfo = TableInfoSettingsService.getInstance().getTableInfo(CacheDataUtils.getInstance().getSelectDbTable());
        setTitle("Config Table " + this.tableInfo.getObj().getName());
        ConfigTableModel model = new ConfigTableModel(this.tableInfo);
        JBTable table = new JBTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configure column editor
        table.getColumn("name").setCellEditor(CellEditorFactory.createTextFieldEditor());
        table.getColumn("type").setCellEditor(CellEditorFactory.createComboBoxEditor(true, GlobalDict.DEFAULT_JAVA_TYPE_LIST));
        table.getColumn("type").setMinWidth(120);
        table.getColumn("comment").setCellEditor(CellEditorFactory.createTextFieldEditor());
        // Other additional columns
        for (ColumnConfig columnConfig : CurrGroupUtils.getCurrColumnConfigGroup().getElementList()) {
            TableColumn column = table.getColumn(columnConfig.getTitle());
            switch (columnConfig.getType()) {
                case TEXT:
                    column.setCellEditor(CellEditorFactory.createTextFieldEditor());
                    break;
                case SELECT:
                    if (StringUtils.isEmpty(columnConfig.getSelectValue())) {
                        column.setCellEditor(CellEditorFactory.createTextFieldEditor());
                    } else {
                        column.setCellEditor(CellEditorFactory.createComboBoxEditor(false, columnConfig.getSelectValue().split(",")));
                    }
                    break;
                case BOOLEAN:
                    column.setCellEditor(CellEditorFactory.createBooleanEditor());
                    break;
                default:
                    break;
            }
        }

        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        this.mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);
        this.mainPanel.setMinimumSize(new Dimension(600, 300));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.mainPanel;
    }

    @Override
    protected void doOKAction() {
        // Save information
        TableInfoSettingsService.getInstance().saveTableInfo(tableInfo);
        super.doOKAction();
    }

}
