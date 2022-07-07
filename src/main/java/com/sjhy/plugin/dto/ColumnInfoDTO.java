package com.sjhy.plugin.dto;

import com.intellij.database.model.DasColumn;
import com.intellij.psi.PsiField;
import com.sjhy.plugin.entity.TypeMapper;
import com.sjhy.plugin.enums.MatchType;
import com.sjhy.plugin.tool.CurrGroupUtils;
import com.sjhy.plugin.tool.DocCommentUtils;
import com.sjhy.plugin.tool.NameUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Column Information Transfer Object
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/14 17:29
 */
@Data
@NoArgsConstructor
public class ColumnInfoDTO {

    public ColumnInfoDTO(PsiField field) {
        this.name = field.getName();
        this.comment = DocCommentUtils.getComment(field.getDocComment());
        this.type = field.getType().getCanonicalText();
        this.custom = false;
        this.ext = new HashMap<>();
    }

    public ColumnInfoDTO(DasColumn column) {
        this.name = NameUtils.getInstance().getJavaName(column.getName());
        this.comment = column.getComment();
        this.type = getJavaType(column.getDataType().toString());
        this.custom = false;
        this.ext = new HashMap<>();
    }

    private String getJavaType(String dbType) {
        for (TypeMapper typeMapper : CurrGroupUtils.getCurrTypeMapperGroup().getElementList()) {
            if (typeMapper.getMatchType() == MatchType.ORDINARY) {
                if (dbType.equalsIgnoreCase(typeMapper.getColumnType())) {
                    return typeMapper.getJavaType();
                }
            } else {
                // Case-insensitive regex pattern
                if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(dbType).matches()) {
                    return typeMapper.getJavaType();
                }
            }
        }
        return "java.lang.Object";
    }

    /**
     * Name
     */
    private String name;
    /**
     * Notes
     */
    private String comment;
    /**
     * All types
     */
    private String type;
    /**
     * Whether the marker is a custom additional column
     */
    private Boolean custom;
    /**
     * Extended data
     */
    private Map<String, Object> ext;
}
