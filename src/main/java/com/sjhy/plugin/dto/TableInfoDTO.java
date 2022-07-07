package com.sjhy.plugin.dto;

import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.util.containers.JBIterable;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.tool.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Form information transfer object
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/14 17:28
 */
@Data
@NoArgsConstructor
public class TableInfoDTO {

    public TableInfoDTO(TableInfoDTO dto, DbTable dbTable) {
        this(dbTable);
        merge(dto, this);
    }

    public TableInfoDTO(TableInfoDTO dto, PsiClass psiClass) {
        this(psiClass);
        merge(dto, this);
    }

    private TableInfoDTO(PsiClass psiClass) {
        this.name = psiClass.getName();
        this.preName = "";
        this.comment = DocCommentUtils.getComment(psiClass.getDocComment());
        this.templateGroupName = "";
        this.savePackageName = "";
        this.savePath = "";
        this.saveModelName = "";
        this.fullColumn = new ArrayList<>();
        for (PsiField field : psiClass.getAllFields()) {
            this.fullColumn.add(new ColumnInfoDTO(field));
        }
    }

    private TableInfoDTO(DbTable dbTable) {
        this.name = NameUtils.getInstance().getClassName(dbTable.getName());
        this.preName = "";
        this.comment = dbTable.getComment();
        this.templateGroupName = "";
        this.savePackageName = "";
        this.savePath = "";
        this.saveModelName = "";
        this.fullColumn = new ArrayList<>();
        // Process all columns
        JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dbTable);
        for (DasColumn column : columns) {
            this.fullColumn.add(new ColumnInfoDTO(column));
        }
    }

    private static void merge(TableInfoDTO oldData, TableInfoDTO newData) {
        if (oldData == null || CollectionUtil.isEmpty(oldData.getFullColumn())) {
            return;
        }
        if (!StringUtils.isEmpty(oldData.getPreName())) {
            newData.preName = oldData.getPreName();
        }
        if (!StringUtils.isEmpty(oldData.getTemplateGroupName())) {
            newData.templateGroupName = oldData.getTemplateGroupName();
        }
        if (!StringUtils.isEmpty(oldData.getSavePackageName())) {
            newData.savePackageName = oldData.getSavePackageName();
        }
        if (!StringUtils.isEmpty(oldData.getSavePath())) {
            newData.savePath = oldData.getSavePath();
        }
        if (!StringUtils.isEmpty(oldData.getSaveModelName())) {
            newData.saveModelName = oldData.getSaveModelName();
        }
        if (CollectionUtil.isEmpty(oldData.getFullColumn())) {
            return;
        }
        // Supplemental custom columns
        for (ColumnInfoDTO oldColumn : oldData.getFullColumn()) {
            if (Boolean.FALSE.equals(oldColumn.getCustom())) {
                continue;
            }
            newData.getFullColumn().add(oldColumn);
        }
        // Keep the order of the old columns
        Map<String, ColumnInfoDTO> map = newData.getFullColumn().stream().collect(Collectors.toMap(ColumnInfoDTO::getName, val -> val));
        List<ColumnInfoDTO> tmpList = new ArrayList<>();
        for (ColumnInfoDTO columnInfo : oldData.getFullColumn()) {
            ColumnInfoDTO newColumn = map.get(columnInfo.getName());
            if (newColumn != null) {
                tmpList.add(newColumn);
            }
        }
        // Supplement remaining columns
        for (ColumnInfoDTO columnInfoDTO : newData.getFullColumn()) {
            if (!tmpList.contains(columnInfoDTO)) {
                tmpList.add(columnInfoDTO);
            }
        }
        // List data replacement
        newData.getFullColumn().clear();
        newData.getFullColumn().addAll(tmpList);
    }


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
    private List<ColumnInfoDTO> fullColumn;
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

    public TableInfo toTableInfo(PsiClass psiClass) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setPsiClassObj(psiClass);
        tableInfo.setName(this.getName());
        tableInfo.setPreName(this.getPreName());
        tableInfo.setTemplateGroupName(this.getTemplateGroupName());
        tableInfo.setSavePackageName(this.getSavePackageName());
        tableInfo.setSavePath(this.getSavePath());
        tableInfo.setComment(this.getComment());
        tableInfo.setSaveModelName(this.getSaveModelName());
        tableInfo.setFullColumn(new ArrayList<>());
        tableInfo.setPkColumn(new ArrayList<>());
        tableInfo.setOtherColumn(new ArrayList<>());
        for (PsiField field : psiClass.getAllFields()) {
            if (PsiClassGenerateUtils.isSkipField(field)) {
                continue;
            }
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setName(field.getName());
            columnInfo.setShortType(field.getType().getPresentableText());
            columnInfo.setType(field.getType().getCanonicalText());
            columnInfo.setComment(DocCommentUtils.getComment(field.getDocComment()));
            columnInfo.setCustom(false);
            tableInfo.getFullColumn().add(columnInfo);
            if (PsiClassGenerateUtils.isPkField(field)) {
                tableInfo.getPkColumn().add(columnInfo);
            } else {
                tableInfo.getOtherColumn().add(columnInfo);
            }
        }
        return tableInfo;
    }

    public TableInfo toTableInfo(DbTable dbTable) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setObj(dbTable);
        tableInfo.setName(this.getName());
        tableInfo.setPreName(this.getPreName());
        tableInfo.setTemplateGroupName(this.getTemplateGroupName());
        tableInfo.setSavePackageName(this.getSavePackageName());
        tableInfo.setSavePath(this.getSavePath());
        tableInfo.setComment(this.getComment());
        tableInfo.setSaveModelName(this.getSaveModelName());
        tableInfo.setFullColumn(new ArrayList<>());
        tableInfo.setPkColumn(new ArrayList<>());
        tableInfo.setOtherColumn(new ArrayList<>());
        // 列
        JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dbTable);
        Map<String, DasColumn> nameToObj = new HashMap<>(columns.size());
        for (DasColumn column : columns) {
            nameToObj.put(NameUtils.getInstance().getJavaName(column.getName()), column);
        }
        for (ColumnInfoDTO dto : this.getFullColumn()) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setObj(nameToObj.get(dto.getName()));
            columnInfo.setName(dto.getName());
            columnInfo.setType(dto.getType());
            // The last section is the short type
            String[] split = dto.getType().split("\\.");
            columnInfo.setShortType(split[split.length - 1]);
            columnInfo.setComment(dto.getComment());
            columnInfo.setCustom(dto.getCustom());
            columnInfo.setExt(dto.getExt());
            tableInfo.getFullColumn().add(columnInfo);
            if (columnInfo.getObj() != null && DasUtil.isPrimary(columnInfo.getObj())) {
                tableInfo.getPkColumn().add(columnInfo);
            } else {
                tableInfo.getOtherColumn().add(columnInfo);
            }
        }
        return tableInfo;
    }

    public static TableInfoDTO valueOf(TableInfo tableInfo) {
        TableInfoDTO dto = new TableInfoDTO();
        dto.setName(tableInfo.getName());
        dto.setTemplateGroupName(tableInfo.getTemplateGroupName());
        dto.setSavePath(tableInfo.getSavePath());
        dto.setPreName(tableInfo.getPreName());
        dto.setComment(tableInfo.getComment());
        dto.setSavePackageName(tableInfo.getSavePackageName());
        dto.setSaveModelName(tableInfo.getSaveModelName());
        dto.setFullColumn(new ArrayList<>());
        // Process column
        for (ColumnInfo columnInfo : tableInfo.getFullColumn()) {
            ColumnInfoDTO columnInfoDTO = new ColumnInfoDTO();
            columnInfoDTO.setName(columnInfo.getName());
            columnInfoDTO.setType(columnInfo.getType());
            columnInfoDTO.setExt(columnInfo.getExt());
            columnInfoDTO.setCustom(columnInfo.getCustom());
            columnInfoDTO.setComment(columnInfo.getComment());
            dto.getFullColumn().add(columnInfoDTO);
        }
        return dto;
    }
}
