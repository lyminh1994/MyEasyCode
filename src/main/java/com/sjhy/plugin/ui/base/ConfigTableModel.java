package com.sjhy.plugin.ui.base;

import com.intellij.util.ui.EditableModel;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.enums.ColumnConfigType;
import com.sjhy.plugin.tool.CurrGroupUtils;
import com.sjhy.plugin.tool.StringUtils;

import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/14 13:41
 */
public class ConfigTableModel extends DefaultTableModel implements EditableModel {

    private TableInfo tableInfo;

    public ConfigTableModel(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
        this.initColumn();
        this.initTableData();
    }

    private void initColumn() {
        addColumn("name");
        addColumn("type");
        addColumn("comment");
        for (ColumnConfig columnConfig : CurrGroupUtils.getCurrColumnConfigGroup().getElementList()) {
            addColumn(columnConfig.getTitle());
        }
    }

    private void initTableData() {
        // delete all columns
        int size = getRowCount();
        for (int i = 0; i < size; i++) {
            super.removeRow(0);
        }
        // render column data
        for (ColumnInfo columnInfo : this.tableInfo.getFullColumn()) {
            List<Object> values = new ArrayList<>();
            values.add(columnInfo.getName());
            values.add(columnInfo.getType());
            values.add(columnInfo.getComment());
            Map<String, Object> ext = columnInfo.getExt();
            if (ext == null) {
                ext = Collections.emptyMap();
            }
            for (ColumnConfig columnConfig : CurrGroupUtils.getCurrColumnConfigGroup().getElementList()) {
                Object obj = ext.get(columnConfig.getTitle());
                if (obj == null) {
                    if (columnConfig.getType() == ColumnConfigType.BOOLEAN) {
                        values.add(false);
                    } else {
                        values.add("");
                    }
                } else {
                    values.add(obj);
                }
            }
            addRow(values.toArray());
        }
    }

    @Override
    public void addRow() {
        Map<String, ColumnInfo> map = this.tableInfo.getFullColumn().stream().collect(Collectors.toMap(ColumnInfo::getName, val -> val));
        String newName = "demo";
        for (int i = 0; map.containsKey(newName); i++) {
            newName = "demo" + i;
        }
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setCustom(true);
        columnInfo.setName(newName);
        columnInfo.setExt(new HashMap<>(16));
        columnInfo.setComment("");
        columnInfo.setShortType("String");
        columnInfo.setType("java.lang.String");
        this.tableInfo.getFullColumn().add(columnInfo);
        // refresh table data
        this.initTableData();
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        ColumnInfo columnInfo = this.tableInfo.getFullColumn().get(row);
        if (columnInfo == null) {
            return;
        }
        // Modification of non-custom data is not allowed
        if (Boolean.FALSE.equals(columnInfo.getCustom()) && column <= 2) {
            return;
        }
        switch (column) {
            case 0:
                String name = (String) value;
                // Column names are not allowed to be empty
                if (StringUtils.isEmpty(name)) {
                    return;
                }
                // Existing duplicate name is not allowed to modify
                boolean existsName = this.tableInfo.getFullColumn().stream().anyMatch(item -> Objects.equals(item.getName(), name));
                if (existsName) {
                    return;
                }
                columnInfo.setName(name);
                break;
            case 1:
                String type = (String) value;
                // Column names are not allowed to be empty
                if (StringUtils.isEmpty(type)) {
                    return;
                }
                columnInfo.setType(type);
                columnInfo.setShortType(type.substring(type.lastIndexOf(".") + 1));
                break;
            case 2:
                columnInfo.setComment((String) value);
                break;
            default:
                ColumnConfig columnConfig = CurrGroupUtils.getCurrColumnConfigGroup().getElementList().get(column - 3);
                if (columnInfo.getExt() == null) {
                    columnInfo.setExt(new HashMap<>(16));
                }
                columnInfo.getExt().put(columnConfig.getTitle(), value);
                break;
        }
        super.setValueAt(value, row, column);
    }

    @Override
    public void removeRow(int row) {
        ColumnInfo columnInfo = this.tableInfo.getFullColumn().get(row);
        if (columnInfo == null) {
            return;
        }
        // Non-custom columns are not allowed to delete
        if (Boolean.FALSE.equals(columnInfo.getCustom())) {
            return;
        }
        this.tableInfo.getFullColumn().remove(row);
        this.initTableData();
    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {
        ColumnInfo columnInfo = this.tableInfo.getFullColumn().remove(oldIndex);
        this.tableInfo.getFullColumn().add(newIndex, columnInfo);
        this.initTableData();
    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
        return false;
    }
}
