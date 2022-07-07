package com.sjhy.plugin.entity;

import com.sjhy.plugin.enums.MatchType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Type Implicit Information
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
@NoArgsConstructor
public class TypeMapper implements AbstractItem<TypeMapper> {
    /**
     * Match type
     */
    private MatchType matchType;
    /**
     * Column type
     */
    private String columnType;
    /**
     * Java type
     */
    private String javaType;

    public TypeMapper(String columnType, String javaType) {
        this.matchType = MatchType.REGEX;
        this.columnType = columnType;
        this.javaType = javaType;
    }

    @Override
    public TypeMapper defaultVal() {
        return new TypeMapper("demo", "java.lang.String");
    }
}
