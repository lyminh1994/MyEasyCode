package com.sjhy.plugin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.database.psi.DbTable;
import lombok.Data;

import java.util.List;

/**
 * Table information
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
public class TableInfo {
    /**
     * Primitive object
     */
    @JsonIgnore
    private DbTable obj;

    /**
     * Primitive object (generated from entity)
     *
     * Note: The actual type is com.intellij.psi.PsiClass. In order to avoid ClassNotFound in velocity reflection, it is written as Object type
     */
    @JsonIgnore
    private Object psiClassObj;

    /**
     * Table name (first letter capitalized)
     */
    private String name;
    /**
     * Table name prefix
     */
    private String preName;
    /**
     * Notes
     */
    private String comment;
    /**
     * Template group name
     */
    private String templateGroupName;
    /**
     * All columns
     */
    private List<ColumnInfo> fullColumn;
    /**
     * Primary key column
     */
    private List<ColumnInfo> pkColumn;
    /**
     * Other columns
     */
    private List<ColumnInfo> otherColumn;
    /**
     * Saved package name
     */
    private String savePackageName;
    /**
     * Save route
     */
    private String savePath;
    /**
     * Saved model name
     */
    private String saveModelName;
}
