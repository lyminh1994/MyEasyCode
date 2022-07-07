package com.sjhy.plugin.entity;

import lombok.Data;

import java.util.List;

/**
 * Template grouping class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
@Data
public class TemplateGroup implements AbstractGroup<TemplateGroup, Template> {
    /**
     * Group Name
     */
    private String name;
    /**
     * Element object
     */
    private List<Template> elementList;
}
