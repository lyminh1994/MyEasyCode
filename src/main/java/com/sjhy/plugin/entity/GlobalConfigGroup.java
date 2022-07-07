package com.sjhy.plugin.entity;

import java.util.List;
import lombok.Data;

/**
 * Global configuration grouping
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/27 13:10
 */
@Data
public class GlobalConfigGroup implements AbstractGroup<GlobalConfigGroup, GlobalConfig> {
    /**
     * Group Name
     */
    private String name;
    /**
     * Collection of element objects
     */
    private List<GlobalConfig> elementList;
}
