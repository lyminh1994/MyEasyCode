package com.sjhy.plugin.entity;

import lombok.Data;

import java.util.List;

/**
 * Column configuration grouping
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
@Data
public class ColumnConfigGroup implements AbstractGroup<ColumnConfigGroup, ColumnConfig> {
    /**
     * Group Name
     */
    private String name;
    /**
     * Element object
     */
    private List<ColumnConfig> elementList;
}
