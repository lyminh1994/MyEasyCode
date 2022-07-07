package com.sjhy.plugin.ui.component;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBCheckBox;
import com.sjhy.plugin.tool.CollectionUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * List Checkbox Component
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/03 17:12
 */
public class ListCheckboxComponent extends JPanel {
    /**
     * Title
     */
    private String title;
    /**
     * List of origins
     */
    private Collection<String> items;
    /**
     * Checkbox List
     */
    private List<JBCheckBox> checkBoxList;

    /**
     * Default constructor
     */
    public ListCheckboxComponent(String title, Collection<String> items) {
        // Use a vertical flow layout
        super(new VerticalFlowLayout());
        this.title = title;
        this.items = items;
        this.init();
    }

    /**
     * Initialization operation
     */
    private void init() {
        JTextPane textPane = new JTextPane();
        textPane.setText(title);
        textPane.setEditable(false);
        add(textPane);
        if (CollectionUtil.isEmpty(items)) {
            return;
        }
        checkBoxList = new ArrayList<>(items.size());
        for (String item : items) {
            JBCheckBox checkBox = new JBCheckBox(item);
            checkBoxList.add(checkBox);
            add(checkBox);
        }
    }

    /**
     * Get the selected element
     *
     * @return Selected element
     */
    public List<String> getSelectedItems() {
        if (CollectionUtil.isEmpty(checkBoxList)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        checkBoxList.forEach(checkBox -> {
            if (checkBox.isSelected()) {
                result.add(checkBox.getText());
            }
        });
        return result;
    }
}
