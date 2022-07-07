package com.sjhy.plugin.actions;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.service.TableInfoSettingsService;
import com.sjhy.plugin.tool.CacheDataUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Action button grouping
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class MainActionGroup extends ActionGroup {
    /**
     * Cache data utility class
     */
    private final CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();

    /**
     * Whether there is no submenu
     */
    private boolean notExistsChildren;

    /**
     * Whether to group buttons
     *
     * @return Whether to hide
     */
    @Override
    public boolean hideIfNoVisibleChildren() {
        return this.notExistsChildren;
    }


    /**
     * Display different submenus on different options according to the right button
     *
     * @param event Event object
     * @return Action group
     */
    @NotNull
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        // Get current project
        Project project = getEventProject(event);
        if (project == null) {
            return getEmptyAnAction();
        }

        //Get the selected PSI element
        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        DbTable selectDbTable = null;
        if (psiElement instanceof DbTable) {
            selectDbTable = (DbTable) psiElement;
        }
        if (selectDbTable == null) {
            return getEmptyAnAction();
        }
        //Get all selected tables
        PsiElement[] psiElements = event.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements == null || psiElements.length == 0) {
            return getEmptyAnAction();
        }
        List<DbTable> dbTableList = new ArrayList<>();
        for (PsiElement element : psiElements) {
            if (!(element instanceof DbTable)) {
                continue;
            }
            DbTable dbTable = (DbTable) element;
            dbTableList.add(dbTable);
        }
        if (dbTableList.isEmpty()) {
            return getEmptyAnAction();
        }

        //Save data to cache
        cacheDataUtils.setDbTableList(dbTableList);
        cacheDataUtils.setSelectDbTable(selectDbTable);
        this.notExistsChildren = false;
        return getMenuList();
    }

    /**
     * Initialize the registration submenu item
     *
     * @return Submenu array
     */
    private AnAction[] getMenuList() {
        String mainActionId = "com.sjhy.easy.code.action.generate";
        String configActionId = "com.sjhy.easy.code.action.config";
        ActionManager actionManager = ActionManager.getInstance();
        // Code generation menu
        AnAction mainAction = actionManager.getAction(mainActionId);
        if (mainAction == null) {
            mainAction = new MainAction("Generate Code");
            actionManager.registerAction(mainActionId, mainAction);
        }
        // Table Configuration Menu
        AnAction configAction = actionManager.getAction(configActionId);
        if (configAction == null) {
            configAction = new ConfigAction("Config Table");
            actionManager.registerAction(configActionId, configAction);
        }
        AnAction clearConfigAction = new AnAction("Clear Config") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                DbTable dbTable = CacheDataUtils.getInstance().getSelectDbTable();
                if (dbTable == null) {
                    return;
                }
                TableInfoSettingsService.getInstance().removeTableInfo(dbTable);
                Messages.showInfoMessage(dbTable.getName() + " table configuration information has been reset successfully", GlobalDict.TITLE_INFO);
            }
        };
        // Back to all menus
        return new AnAction[]{mainAction, configAction, clearConfigAction};
    }


    /**
     * Get empty menu group
     *
     * @return Empty menu group
     */
    private AnAction[] getEmptyAnAction() {
        this.notExistsChildren = true;
        return AnAction.EMPTY_ARRAY;
    }
}
