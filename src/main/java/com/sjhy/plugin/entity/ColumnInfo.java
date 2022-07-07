package com.sjhy.plugin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.database.model.DasColumn;
import lombok.Data;

import java.util.Map;

/**
 * Column information
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
public class ColumnInfo {
    /**
     * Primitive object
     */
    @JsonIgnore
    private DasColumn obj;
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
     * Short type
     */
    private String shortType;
    /**
     * Whether the marker is a custom additional column
     */
    private Boolean custom;
    /**
     * Extended data
     */
    private Map<String, Object> ext;
}
