package com.sjhy.plugin.entity;

import java.util.List;
import lombok.Data;

/**
 * Typemap grouping
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
public class TypeMapperGroup implements AbstractGroup<TypeMapperGroup, TypeMapper> {
    /**
     * Group Name
     */
    private String name;
    /**
     * Element object
     */
    private List<TypeMapper> elementList;
}
