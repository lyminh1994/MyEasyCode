package com.sjhy.plugin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Global configuration entity class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/27 13:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalConfig implements AbstractEditorItem<GlobalConfig> {
    /**
     * Name
     */
    private String name;
    /**
     * Value
     */
    private String value;

    @Override
    public GlobalConfig defaultVal() {
        return new GlobalConfig("demo", "value");
    }

    @Override
    public void changeFileName(String name) {
        this.name = name;
    }

    @Override
    public String fileName() {
        return this.name;
    }

    @Override
    public void changeFileContent(String content) {
        this.value = content;
    }

    @Override
    public String fileContent() {
        return this.value;
    }
}
