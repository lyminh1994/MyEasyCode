package com.sjhy.plugin.service;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiClass;
import com.sjhy.plugin.dto.TableInfoSettingsDTO;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.service.impl.TableInfoSettingsServiceImpl;
import com.sjhy.plugin.tool.ProjectUtils;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/14 15:16
 */
public interface TableInfoSettingsService extends PersistentStateComponent<TableInfoSettingsDTO> {
    /**
     * Get instance
     *
     * @return {@link SettingsStorageService}
     */
    static TableInfoSettingsService getInstance() {
        return ServiceManager.getService(ProjectUtils.getCurrProject(), TableInfoSettingsServiceImpl.class);
    }

    /**
     * Get form information
     *
     * @param dbTable Database Table
     * @return {@link TableInfo}
     */
    TableInfo getTableInfo(DbTable dbTable);

    /**
     * Get table information
     *
     * @param psiClass Psi class
     * @return {@link TableInfo}
     */
    TableInfo getTableInfo(PsiClass psiClass);

    /**
     * Save table information
     *
     * @param tableInfo Table information
     */
    void saveTableInfo(TableInfo tableInfo);

    /**
     * Reset table information
     *
     * @param dbTable Database Table
     */
    void resetTableInfo(DbTable dbTable);

    /**
     * 删除表信息
     *
     * @param dbTable 数据库表
     */
    void removeTableInfo(DbTable dbTable);
}
