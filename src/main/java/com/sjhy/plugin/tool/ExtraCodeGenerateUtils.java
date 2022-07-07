package com.sjhy.plugin.tool;

import com.sjhy.plugin.dto.GenerateOptions;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.service.impl.CodeGenerateServiceImpl;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Additional code generation tools
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/11/01 10:11
 */
public class ExtraCodeGenerateUtils {
    /**
     * Code generation service
     */
    private CodeGenerateServiceImpl codeGenerateService;
    /**
     * Table info object
     */
    private TableInfo tableInfo;
    /**
     * Build configuration
     */
    private GenerateOptions generateOptions;

    public ExtraCodeGenerateUtils(CodeGenerateServiceImpl codeGenerateService, TableInfo tableInfo, GenerateOptions generateOptions) {
        this.codeGenerateService = codeGenerateService;
        this.tableInfo = tableInfo;
        this.generateOptions = generateOptions;
    }

    /**
     * Generate code
     *
     * @param templateName Template name
     * @param param        Additional parameters
     */
    public void run(String templateName, Map<String, Object> param) {
        // Get the template
        Template currTemplate = null;
        for (Template template : CurrGroupUtils.getCurrTemplateGroup().getElementList()) {
            if (Objects.equals(template.getName(), templateName)) {
                currTemplate = template;
            }
        }
        if (currTemplate == null) {
            return;
        }
        // Generate code
        codeGenerateService.generate(Collections.singletonList(currTemplate), Collections.singletonList(this.tableInfo), this.generateOptions, param);
    }
}
