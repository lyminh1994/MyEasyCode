package com.sjhy.plugin.ui.component;

import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.EditableModel;
import com.sjhy.plugin.entity.AbstractItem;
import com.sjhy.plugin.factory.AbstractItemFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Form component
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/10 09:52
 */
public class TableComponent<T extends AbstractItem<T>> extends DefaultTableModel implements EditableModel {
    /**
     * Column information
     */
    private List<Column<T>> columns;
    /**
     * Table data
     */
    private List<T> dataList;
    /**
     * Sheet
     */
    @Getter
    private JBTable table;

    private Class<T> cls;

    public TableComponent(@NonNull List<Column<T>> columns, @NonNull List<T> dataList, Class<T> cls) {
        this.columns = columns;
        this.dataList = dataList;
        this.cls = cls;
        this.initColumnName();
        this.initTable();
        this.setDataList(dataList);
    }

    public JComponent createPanel() {
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(this.table);
        return decorator.createPanel();
    }

    private void initColumnName() {
        for (Column<T> column : this.columns) {
            addColumn(column.name);
        }
    }

    private void initTable() {
        this.table = new JBTable(this);
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Specified editor
        for (Column<T> column : this.columns) {
            if (column.editor != null) {
                this.table.getColumn(column.name).setCellEditor(column.editor);
            }
        }
    }

    private Vector<String> toObj(T e) {
        Vector<String> vector = new Vector<>();
        this.columns.stream().map(item -> item.getFun.apply(e)).forEach(vector::add);
        return vector;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
        // Clear data
        removeAllRow();
        for (T entity : this.dataList) {
            addRow(entity);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (row < this.dataList.size()) {
            super.setValueAt(value, row, column);
            T obj = this.dataList.get(row);
            this.columns.get(column).getSetFun().accept(obj, (String) value);
        }
    }

    /**
     * Remove all lines
     */
    public void removeAllRow() {
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            super.removeRow(0);
        }
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        this.dataList.remove(row);
    }

    public void addRow(T entity) {
        addRow(toObj(entity));
    }

    @Override
    public void addRow() {
        T entity = AbstractItemFactory.createDefaultVal(cls);
        this.dataList.add(entity);
        addRow(entity);
    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {
        super.moveRow(oldIndex, oldIndex, newIndex);
        T remove = this.dataList.remove(oldIndex);
        this.dataList.add(newIndex, remove);
    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
        return true;
    }


    @Data
    @AllArgsConstructor
    public static class Column<T> {
        /**
         * Column name
         */
        private String name;
        /**
         * Get method
         */
        private Function<T, String> getFun;
        /**
         * Set method
         */
        private BiConsumer<T, String> setFun;
        /**
         * Column editor
         */
        private TableCellEditor editor;
    }
}
