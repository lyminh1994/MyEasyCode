package com.sjhy.plugin.ui.component;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.entity.AbstractItem;
import com.sjhy.plugin.tool.StringUtils;
import com.sjhy.plugin.ui.base.InputExistsValidator;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Group editing components
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/10 14:13
 */
public class GroupNameComponent<E extends AbstractItem<E>, T extends AbstractGroup<T, E>> {

    private Consumer<T> switchGroupConsumer;

    /**
     * Current group
     */
    private String currGroupName;

    @Getter
    private JPanel panel;

    private ComboBox<String> groupComboBox;

    private boolean refresh;

    private Map<String, T> groupMap;

    public GroupNameComponent(Consumer<T> switchGroupConsumer, Map<String, T> groupMap) {
        this.switchGroupConsumer = switchGroupConsumer;
        this.groupMap = groupMap;
        this.currGroupName = groupMap.keySet().stream().findFirst().orElse(null);
        this.init();
    }

    /**
     * Enter group name
     *
     * @param initValue Initial value
     * @param consumer  Consumption group name
     */
    private void inputGroupName(String initValue, Consumer<String> consumer) {
        String value = Messages.showInputDialog("Group Name:", "Input Group Name:", Messages.getQuestionIcon(), initValue, new InputExistsValidator(groupMap.keySet()));
        if (StringUtils.isEmpty(value)) {
            return;
        }
        consumer.accept(value);
    }

    private AnAction copyAction() {
        return new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                inputGroupName(currGroupName + "Copy", groupName -> {
                    // Make a copy, rename
                    T cloneObj = groupMap.get(currGroupName).cloneObj();
                    cloneObj.setName(groupName);
                    // Add group
                    groupMap.put(groupName, cloneObj);
                    // Switch group
                    switchGroupConsumer.accept(cloneObj);
                });
            }
        };
    }

    private AnAction addAction() {
        return new AnAction(AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                inputGroupName("GroupName", groupName -> {
                    T obj = groupMap.get(currGroupName).cloneObj();
                    E item = obj.defaultChild();
                    obj.setName(groupName);
                    obj.setElementList(new ArrayList<>());
                    obj.getElementList().add(item);
                    groupMap.put(groupName, obj);
                    // Switch group
                    switchGroupConsumer.accept(obj);
                });
            }
        };
    }

    private AnAction removeAction() {
        return new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                groupMap.remove(currGroupName);
                // Switch group
                switchGroupConsumer.accept(groupMap.get(GlobalDict.DEFAULT_GROUP_NAME));
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(!GlobalDict.DEFAULT_GROUP_NAME.equals(currGroupName));
            }
        };
    }

    private void init() {
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.panel.add(new JBLabel("Group Name:"));
        this.groupComboBox = new ComboBox<>(this.groupMap.keySet().toArray(new String[0]));
        this.panel.add(this.groupComboBox);
        // Group operation
        DefaultActionGroup groupAction = new DefaultActionGroup(Arrays.asList(this.copyAction(), this.addAction(), this.removeAction()));
        ActionToolbar groupActionToolbar = ActionManager.getInstance().createActionToolbar("Group Toolbar", groupAction, true);
        this.panel.add(groupActionToolbar.getComponent());
        this.groupComboBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (refresh) {
                    return;
                }
                String selectedItem = (String) groupComboBox.getSelectedItem();
                if (StringUtils.isEmpty(selectedItem)) {
                    return;
                }
                switchGroupConsumer.accept(groupMap.get(selectedItem));
            }
        });
    }

    /**
     * Update group information
     *
     * @param groupMap All group information
     */
    public void setGroupMap(Map<String, T> groupMap) {
        this.groupMap = groupMap;
        this.refreshGroupComboBox();
    }

    /**
     * Refresh the following boxes
     */
    private void refreshGroupComboBox() {
        try {
            this.refresh = true;
            this.groupComboBox.removeAllItems();
            for (String item : this.groupMap.keySet()) {
                this.groupComboBox.addItem(item);
            }
        } finally {
            this.refresh = false;
        }
    }

    /**
     * Toggle selected group
     *
     * @param currGroupName Group name
     */
    public void setCurrGroupName(String currGroupName) {
        this.currGroupName = currGroupName;
        try {
            this.refresh = true;
            this.groupComboBox.setSelectedItem(this.currGroupName);
        } finally {
            this.refresh = false;
        }
    }
}
