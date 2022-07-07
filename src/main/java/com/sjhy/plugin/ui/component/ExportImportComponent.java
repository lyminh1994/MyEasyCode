package com.sjhy.plugin.ui.component;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.ex.MultiLineLabel;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.service.ExportImportSettingsService;
import com.sjhy.plugin.service.SettingsStorageService;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.CollectionUtil;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.tool.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Export import component
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/12 10:35
 */
public class ExportImportComponent {
    /**
     * Export button
     */
    private JButton exportBtn;
    /**
     * Import button
     */
    private JButton importBtn;
    /**
     * Export import service
     */
    private ExportImportSettingsService service;

    /**
     * Import success callback
     */
    private Runnable callback;

    public ExportImportComponent(JButton exportBtn, JButton importBtn, ExportImportSettingsService service, Runnable callback) {
        this.exportBtn = exportBtn;
        this.importBtn = importBtn;
        this.service = service;
        this.callback = callback;
        this.init();
    }

    private void init() {
        this.exportBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlerExportAction();
            }
        });
        this.importBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlerImportAction();
            }
        });
    }

    /**
     * Handling export actions
     */
    private void handlerExportAction() {
        // Make a copy to avoid tampering
        SettingsStorageDTO settingsStorage = CloneUtils.cloneByJson(SettingsStorageService.getSettingsStorage());
        // Create a main panel with one row and four columns
        JPanel mainPanel = new JPanel(new GridLayout(1, 4));
        // Type Mapper
        ListCheckboxComponent typeMapperPanel = new ListCheckboxComponent("Type Mapper", settingsStorage.getTypeMapperGroupMap().keySet());
        mainPanel.add(typeMapperPanel);
        // Template
        ListCheckboxComponent templatePanel = new ListCheckboxComponent("Template", settingsStorage.getTemplateGroupMap().keySet());
        mainPanel.add(templatePanel);
        // Column Config
        ListCheckboxComponent columnConfigPanel = new ListCheckboxComponent("Column Config", settingsStorage.getColumnConfigGroupMap().keySet());
        mainPanel.add(columnConfigPanel);
        // GlobalConfig
        ListCheckboxComponent globalConfigPanel = new ListCheckboxComponent("Global Config", settingsStorage.getGlobalConfigGroupMap().keySet());
        mainPanel.add(globalConfigPanel);
        // Build dialog
        DialogBuilder dialogBuilder = new DialogBuilder(ProjectUtils.getCurrProject());
        dialogBuilder.setTitle(GlobalDict.TITLE_INFO);
        dialogBuilder.setNorthPanel(new MultiLineLabel("Please select a configuration group to export："));
        dialogBuilder.setCenterPanel(mainPanel);
        dialogBuilder.addActionDescriptor(dialogWrapper -> new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSelected(typeMapperPanel, templatePanel, columnConfigPanel, globalConfigPanel)) {
                    Messages.showWarningDialog("Choose at least one template group!", GlobalDict.TITLE_INFO);
                    return;
                }
                // Filter data
                filterSelected(typeMapperPanel, settingsStorage.getTypeMapperGroupMap());
                filterSelected(templatePanel, settingsStorage.getTemplateGroupMap());
                filterSelected(columnConfigPanel, settingsStorage.getColumnConfigGroupMap());
                filterSelected(globalConfigPanel, settingsStorage.getGlobalConfigGroupMap());
                // Close and exit
                dialogWrapper.close(DialogWrapper.OK_EXIT_CODE);
                service.exportConfig(settingsStorage);
            }
        });
        // Display window
        dialogBuilder.show();
    }

    /**
     * Determine whether to select
     *
     * @param checkboxPanels Checkbox panel
     * @return Is it selected
     */
    private boolean isSelected(@NotNull ListCheckboxComponent... checkboxPanels) {
        for (ListCheckboxComponent checkboxPanel : checkboxPanels) {
            if (!CollectionUtil.isEmpty(checkboxPanel.getSelectedItems())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filter selected data
     *
     * @param checkboxPanel Selected panel
     * @param map           Map to filter
     */
    private void filterSelected(ListCheckboxComponent checkboxPanel, Map<String, ?> map) {
        List<String> selectedItems = checkboxPanel.getSelectedItems();
        map.keySet().removeIf(item -> !selectedItems.contains(item));
    }

    private void handlerImportAction() {
        SettingsStorageDTO localSettings = SettingsStorageService.getSettingsStorage();
        SettingsStorageDTO remoteSettings = service.importConfig();
        if (remoteSettings == null) {
            return;
        }
        // Overwrite, abandon, and rename groups with the same name
        // Create the main panel
        JPanel mainPanel = new JPanel(new VerticalFlowLayout());
        List<Handler> allHandlerList = new ArrayList<>();
        addRadioComponent(allHandlerList, "TypeMapper", localSettings.getTypeMapperGroupMap(), remoteSettings.getTypeMapperGroupMap());
        addRadioComponent(allHandlerList, "Template", localSettings.getTemplateGroupMap(), remoteSettings.getTemplateGroupMap());
        addRadioComponent(allHandlerList, "ColumnConfig", localSettings.getColumnConfigGroupMap(), remoteSettings.getColumnConfigGroupMap());
        addRadioComponent(allHandlerList, "GlobalConfig", localSettings.getGlobalConfigGroupMap(), remoteSettings.getGlobalConfigGroupMap());
        for (Handler handler : allHandlerList) {
            if (handler.getRadioComponent() != null) {
                mainPanel.add(handler.getRadioComponent());
            }
        }
        // If there is no grouping that needs to be selected, the Dialog will not be built
        boolean anyMatch = allHandlerList.stream().anyMatch(item -> item.getRadioComponent() != null);
        if (!anyMatch) {
            // Execute per processor
            for (Handler handler : allHandlerList) {
                handler.execute();
            }
            // Execute callback
            if (callback != null) {
                callback.run();
            }
            return;
        }
        // Build dialog
        DialogBuilder dialogBuilder = new DialogBuilder(ProjectUtils.getCurrProject());
        dialogBuilder.setTitle(GlobalDict.TITLE_INFO);
        dialogBuilder.setNorthPanel(new MultiLineLabel("Please select how to handle duplicate configurations: "));
        dialogBuilder.setCenterPanel(mainPanel);
        dialogBuilder.addActionDescriptor(dialogWrapper -> new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Execute per processor
                for (Handler handler : allHandlerList) {
                    handler.execute();
                }
                // Execute callback
                if (callback != null) {
                    callback.run();
                }
                // Close and exit
                dialogWrapper.close(DialogWrapper.OK_EXIT_CODE);
                Messages.showInfoMessage("Import completed!", GlobalDict.TITLE_INFO);
            }
        });
        // Display window
        dialogBuilder.show();
    }

    private <T extends AbstractGroup> void addRadioComponent(List<Handler> allHandlerList, String groupName, Map<String, T> localMap, Map<String, T> remoteMap) {
        if (CollectionUtil.isEmpty(remoteMap)) {
            return;
        }
        for (String key : remoteMap.keySet()) {
            if (localMap.containsKey(key)) {
                ListRadioComponent listRadioComponent = new ListRadioComponent(groupName + "->" + key, Stream.of(Operator.values()).map(item -> StringUtils.capitalize(item.name())).collect(Collectors.toList()));
                allHandlerList.add(new Handler<>(listRadioComponent, localMap, remoteMap, key));
            } else {
                allHandlerList.add(new Handler<>(null, localMap, remoteMap, key));
            }
        }
    }

    private static class Handler<T extends AbstractGroup> {
        @Getter
        private ListRadioComponent radioComponent;

        private Map<String, T> localMap;

        private Map<String, T> remoteMap;

        private String name;

        Handler(ListRadioComponent radioComponent, Map<String, T> localMap, Map<String, T> remoteMap, String name) {
            this.radioComponent = radioComponent;
            this.localMap = localMap;
            this.remoteMap = remoteMap;
            this.name = name;
        }

        void execute() {
            Operator operator = Operator.COVER;
            if (radioComponent != null) {
                String selectedItem = radioComponent.getSelectedItem();
                if (selectedItem != null) {
                    operator = Operator.valueOf(selectedItem.toUpperCase());
                }
            }
            switch (operator) {
                case COVER:
                    localMap.put(name, remoteMap.get(name));
                    break;
                case RENAME:
                    String newName = name;
                    for (int i = 0; localMap.containsKey(newName); i++) {
                        newName = name + i;
                    }
                    T item = remoteMap.get(name);
                    item.setName(newName);
                    localMap.put(newName, item);
                    break;
                default:
                    break;
            }
        }
    }

    public enum Operator {
        /**
         * Cover
         */
        COVER,
        /**
         * Rename
         */
        RENAME,
        /**
         * Throw away
         */
        DISCARD
    }
}
