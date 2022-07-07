package com.sjhy.plugin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Template info class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Template implements AbstractEditorItem<Template> {
    /**
     * Template name
     */
    private String name;
    /**
     * Template code
     */
    private String code;

    @Override
    public Template defaultVal() {
        return new Template("demo", "template");
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
        this.code = content;
    }

    @Override
    public String fileContent() {
        return this.code;
    }
}
