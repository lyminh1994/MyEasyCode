package com.sjhy.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.sjhy.plugin.ui.ConfigTableDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Table Configuration Menu
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class ConfigAction extends AnAction {
    /**
     * Construction method
     *
     * @param text Menu name
     */
    ConfigAction(@Nullable String text) {
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
        new ConfigTableDialog().show();
    }
}
