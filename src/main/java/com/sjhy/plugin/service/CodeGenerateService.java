package com.sjhy.plugin.service;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.sjhy.plugin.dto.GenerateOptions;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Code generation service, Project level Service
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/02 12:48
 */
public interface CodeGenerateService {
    /**
     * Get instance object
     *
     * @param project Project object
     * @return Instance object
     */
    static CodeGenerateService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, CodeGenerateService.class);
    }

    /**
     * Generate
     *
     * @param templates       Template
     * @param generateOptions Build options
     */
    void generate(Collection<Template> templates, GenerateOptions generateOptions);

    /**
     * Generate code
     *
     * @param template  Template
     * @param tableInfo Table info object
     * @return Generated code
     */
    String generate(Template template, TableInfo tableInfo);
}
