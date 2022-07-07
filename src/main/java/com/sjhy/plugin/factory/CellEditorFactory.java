package com.sjhy.plugin.factory;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.stream.Stream;

/**
 * Form editor build factory class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/10 13:38
 */
public class CellEditorFactory {

    private CellEditorFactory() {
    }

    /**
     * Create dropdown editor
     *
     * @param editable Editable
     * @return {@link TableCellEditor}
     */
    public static TableCellEditor createComboBoxEditor(boolean editable, Class<? extends Enum> enumCls) {
        Enum[] enumConstants = enumCls.getEnumConstants();
        return createComboBoxEditor(editable, Stream.of(enumConstants).map(Enum::name).toArray(value -> new String[enumConstants.length]));
    }

    /**
     * Create dropdown editor
     *
     * @param editable Editable
     * @param items    Options
     * @return {@link TableCellEditor}
     */
    public static TableCellEditor createComboBoxEditor(boolean editable, String... items) {
        ComboBox<String> comboBox = new ComboBox<>(items);
        comboBox.setEditable(editable);
        // Color matching
        if (comboBox.getPopup() != null) {
            comboBox.getPopup().getList().setBackground(JBColor.WHITE);
            comboBox.getPopup().getList().setForeground(JBColor.GREEN);
        }
        if (!editable) {
            transmitFocusEvent(comboBox);
        }
        return new DefaultCellEditor(comboBox);
    }

    public static TableCellEditor createBooleanEditor() {
        return new BooleanTableCellEditor();
    }

    /**
     * Create text box editor
     *
     * @return {@link TableCellEditor}
     */
    public static TableCellEditor createTextFieldEditor() {
        JBTextField textField = new JBTextField();
        transmitFocusEvent(textField);
        return new DefaultCellEditor(textField);
    }

    /**
     * Pass lost focus event
     *
     * @param component Components
     */
    private static void transmitFocusEvent(JComponent component) {
        component.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // document why this method is empty
            }

            @Override
            public void focusLost(FocusEvent e) {
                // When the focus is lost, an event notification is sent to the upper layer, so that the value of the table can be written back normally
                ActionListener[] actionListeners = component.getListeners(ActionListener.class);
                if (actionListeners == null) {
                    return;
                }
                for (ActionListener actionListener : actionListeners) {
                    actionListener.actionPerformed(null);
                }
            }
        });
    }

}
