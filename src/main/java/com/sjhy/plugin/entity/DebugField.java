package com.sjhy.plugin.entity;

import lombok.Data;

/**
 * Debug field entity class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/03 11:09
 */
@Data
public class DebugField {
    /**
     * Field name
     */
    private String name;
    /**
     * Field Type
     */
    private Class<?> type;
    /**
     * Field value
     */
    private String value;
}
