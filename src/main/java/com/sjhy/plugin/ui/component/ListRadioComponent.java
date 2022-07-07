package com.sjhy.plugin.ui.component;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBRadioButton;
import com.sjhy.plugin.tool.CollectionUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * List radio component
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/12 11:12
 */
public class ListRadioComponent extends JPanel {
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
    private List<JBRadioButton> radioList;

    /**
     * Default constructor
     */
    public ListRadioComponent(String title, Collection<String> items) {
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
        JPanel radioPanel = new JPanel(new GridLayout(1, 4));
        if (CollectionUtil.isEmpty(items)) {
            return;
        }
        this.radioList = new ArrayList<>(items.size());
        ButtonGroup buttonGroup = new ButtonGroup();
        for (String item : items) {
            JBRadioButton radioButton = new JBRadioButton(item);
            this.radioList.add(radioButton);
            radioPanel.add(radioButton);
            buttonGroup.add(radioButton);
        }
        add(radioPanel);
        // The first one is selected by default
        this.radioList.get(0).setSelected(true);
    }

    /**
     * Get the selected element
     *
     * @return Selected element
     */
    public String getSelectedItem() {
        if (CollectionUtil.isEmpty(this.radioList)) {
            return null;
        }
        for (JBRadioButton radioButton : this.radioList) {
            if (radioButton.isSelected()) {
                return radioButton.getText();
            }
        }
        return null;
    }
}
