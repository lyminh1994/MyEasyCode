package com.sjhy.plugin.entity;

import lombok.Data;

/**
 * Debug method entity class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/03 11:10
 */
@Data
public class DebugMethod {
    /**
     * Method name
     */
    private String name;
    /**
     * Method description
     */
    private String desc;
    /**
     * The value obtained by executing the method
     */
    private Object value;
}
