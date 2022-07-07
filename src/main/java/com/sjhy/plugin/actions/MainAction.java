package com.sjhy.plugin.actions;

import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.ui.JBUI;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.TypeMapper;
import com.sjhy.plugin.enums.MatchType;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.tool.CurrGroupUtils;
import com.sjhy.plugin.tool.StringUtils;
import com.sjhy.plugin.ui.SelectSavePath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Code generation menu
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class MainAction extends AnAction {
    /**
     * Construction method
     *
     * @param text Menu name
     */
    MainAction(@Nullable String text) {
        super(text);
    }

    /**
     * Approach
     *
     * @param event Event object
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        // Check Type Mapping
        if (!typeValidator(project, CacheDataUtils.getInstance().getSelectDbTable())) {
            // Failed to open the window
            return;
        }
        //Start processing
        new SelectSavePath(event.getProject()).show();
    }


    /**
     * Type checking, if there is an unknown type, it is used to de-condition the type
     *
     * @param dbTable Raw table object
     * @return Is it verified
     */
    private boolean typeValidator(Project project, DbTable dbTable) {
        // Process all columns
        JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dbTable);
        List<TypeMapper> typeMapperList = CurrGroupUtils.getCurrTypeMapperGroup().getElementList();

        // Simply record the number of error pop-ups to avoid repeated errors
        Set<String> errorCount = new HashSet<>();

        FLAG:
        for (DasColumn column : columns) {
            String typeName = column.getDataType().getSpecification();
            for (TypeMapper typeMapper : typeMapperList) {
                try {
                    if (typeMapper.getMatchType() == MatchType.ORDINARY) {
                        if (typeName.equalsIgnoreCase(typeMapper.getColumnType())) {
                            continue FLAG;
                        }
                    } else {
                        // Case-insensitive regex pattern
                        if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(typeName).matches()) {
                            continue FLAG;
                        }
                    }
                } catch (PatternSyntaxException e) {
                    if (!errorCount.contains(typeMapper.getColumnType())) {
                        Messages.showWarningDialog(
                                "Type map \"" + typeMapper.getColumnType() + "\" there are grammatical errors, please correct them in time. Error message: " + e.getMessage(),
                                GlobalDict.TITLE_INFO);
                        errorCount.add(typeMapper.getColumnType());
                    }
                }
            }
            // Type not found, prompt user to select input type
            new Dialog(project, typeName).showAndGet();
        }
        return true;
    }

    public static class Dialog  extends DialogWrapper {

        private String typeName;

        private JPanel mainPanel;

        private ComboBox<String> comboBox;

        protected Dialog(@Nullable Project project, String typeName) {
            super(project);
            this.typeName = typeName;
            this.initPanel();
        }

        private void initPanel() {
            setTitle(GlobalDict.TITLE_INFO);
            String msg = String.format("Database type %s, no mapping relationship found, please enter the type you want to convert?", typeName);
            JLabel label = new JLabel(msg);
            this.mainPanel = new JPanel(new BorderLayout());
            this.mainPanel.setBorder(JBUI.Borders.empty(5, 10, 7, 10));
            mainPanel.add(label, BorderLayout.NORTH);
            this.comboBox = new ComboBox<>(GlobalDict.DEFAULT_JAVA_TYPE_LIST);
            this.comboBox.setEditable(true);
            this.mainPanel.add(this.comboBox, BorderLayout.CENTER);
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            return this.mainPanel;
        }

        @Override
        protected void doOKAction() {
            super.doOKAction();
            String selectedItem = (String) this.comboBox.getSelectedItem();
            if (StringUtils.isEmpty(selectedItem)) {
                return;
            }
            TypeMapper typeMapper = new TypeMapper();
            typeMapper.setMatchType(MatchType.ORDINARY);
            typeMapper.setJavaType(selectedItem);
            typeMapper.setColumnType(typeName);
            CurrGroupUtils.getCurrTypeMapperGroup().getElementList().add(typeMapper);
        }
    }
}
