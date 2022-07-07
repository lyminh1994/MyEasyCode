package com.sjhy.plugin.entity;

import com.sjhy.plugin.enums.ColumnConfigType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Column configuration information
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
@NoArgsConstructor
public class ColumnConfig implements AbstractItem<ColumnConfig> {
    /**
     * Title
     */
    private String title;
    /**
     * Type
     */
    private ColumnConfigType type;
    /**
     * Optional values, comma separated
     */
    private String selectValue;

    public ColumnConfig(String title, ColumnConfigType type) {
        this.title = title;
        this.type = type;
    }

    public ColumnConfig(String title, ColumnConfigType type, String selectValue) {
        this.title = title;
        this.type = type;
        this.selectValue = selectValue;
    }

    @Override
    public ColumnConfig defaultVal() {
        return new ColumnConfig("demo", ColumnConfigType.TEXT);
    }
}
