package com.sjhy.plugin.dto;

import com.intellij.database.model.DasNamespace;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbTable;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.tool.ReflectionUtils;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

/**
 * Form information set transfer object
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/14 17:40
 */
@Data
public class TableInfoSettingsDTO {
    private Map<String, TableInfoDTO> tableInfoMap;

    public TableInfoSettingsDTO() {
        this.tableInfoMap = new TreeMap<>();
    }

    private String generateKey(DbTable dbTable) {
        // Recursively add three layers of names as keys, the first layer is the table name, the second layer is the namespace name, and the third layer is the database name
        StringBuilder builder = new StringBuilder();
        DbElement element = dbTable;
        for (int i = 0; i < 3; i++) {
            String name = element.getName();
            if (builder.length() > 0) {
                // Add separator
                builder.insert(0, ".");
            }
            builder.insert(0, name);
            try {
                Method method = ReflectionUtils.getDeclaredMethod(element.getClass(), "getParent");
                if (method == null) {
                    break;
                }
                element = (DbElement) method.invoke(element);
            } catch (IllegalAccessException | InvocationTargetException e) {
                break;
            }
            // Not all databases have three layers. For example, MySQL has only two layers. If the last layer is not a Namespace, do not continue to get
            if (!(element instanceof DasNamespace)) {
                break;
            }
        }
        return builder.toString();
    }

    private String generateKey(PsiClass psiClass) {
        return psiClass.getQualifiedName();
    }
    /**
     * Meter reading information
     *
     * @param psiClass Psi class
     * @return {@link TableInfo}
     */
    @SuppressWarnings("Duplicates")
    public TableInfo readTableInfo(PsiClass psiClass) {
        String key = generateKey(psiClass);
        TableInfoDTO dto = this.tableInfoMap.get(key);
        dto = new TableInfoDTO(dto, psiClass);
        this.tableInfoMap.put(key, dto);
        return dto.toTableInfo(psiClass);
    }

    /**
     * Meter reading information
     *
     * @param dbTable Database Table
     * @return {@link TableInfo}
     */
    @SuppressWarnings("Duplicates")
    public TableInfo readTableInfo(DbTable dbTable) {
        String key = generateKey(dbTable);
        TableInfoDTO dto = this.tableInfoMap.get(key);
        dto = new TableInfoDTO(dto, dbTable);
        this.tableInfoMap.put(key, dto);
        return dto.toTableInfo(dbTable);
    }

    /**
     * Save table information
     *
     * @param tableInfo Table information
     */
    public void saveTableInfo(TableInfo tableInfo) {
        if (tableInfo == null) {
            return;
        }
        DbTable dbTable = tableInfo.getObj();
        String key;
        if (dbTable != null) {
            key = generateKey(dbTable);
        } else if (tableInfo.getPsiClassObj() != null) {
            key = generateKey((PsiClass) tableInfo.getPsiClassObj());
        } else {
            Messages.showInfoMessage(tableInfo.getName() + "表配置信息保存失败", GlobalDict.TITLE_INFO);
            return;
        }
        this.tableInfoMap.put(key, TableInfoDTO.valueOf(tableInfo));
    }

    /**
     * Reset table information
     *
     * @param dbTable Database Table
     */
    public void resetTableInfo(DbTable dbTable) {
        String key = generateKey(dbTable);
        this.tableInfoMap.put(key, new TableInfoDTO(null, dbTable));
    }

    /**
     * 删除表信息
     *
     * @param dbTable 数据库表
     */
    public void removeTableInfo(DbTable dbTable) {
        String key = generateKey(dbTable);
        this.tableInfoMap.remove(key);
    }
}
