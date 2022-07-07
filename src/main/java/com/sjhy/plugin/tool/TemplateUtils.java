package com.sjhy.plugin.tool;

import com.sjhy.plugin.entity.GlobalConfig;
import com.sjhy.plugin.entity.Template;

import java.util.Collection;
import java.util.Collections;

/**
 * Template tools, mainly used to preprocess templates
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/01 15:07
 */
public final class TemplateUtils {
    /**
     * Instance object creation is not allowed
     */
    private TemplateUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Inject global variables into templates
     *
     * @param template      Template
     * @param globalConfigs Global variable
     * @return Processed template
     */
    public static String addGlobalConfig(String template, Collection<GlobalConfig> globalConfigs) {
        if (CollectionUtil.isEmpty(globalConfigs)) {
            return template;
        }
        for (GlobalConfig globalConfig : globalConfigs) {
            String name = globalConfig.getName();
            // Regular replacement character escape processing
            String value = globalConfig.getValue().replace("$", "\\$" );

            // 将不带{}的变量加上{}
            template = template.replaceAll("\\$!?" + name + "(\\W)", "\\$!{" + name + "}$1");
            // Uniform replacement
            template = template.replaceAll("\\$!?\\{" + name + "}", value);
        }
        return template;
    }

    /**
     * Inject global variables into templates
     *
     * @param template      Template object
     * @param globalConfigs Global variable
     */
    public static void addGlobalConfig(Template template, Collection<GlobalConfig> globalConfigs) {
        if (template == null || StringUtils.isEmpty(template.getCode())) {
            return;
        }
        // Add a newline symbol after the template to prevent adding global variables at the end of the template to cause unmatched problems
        template.setCode(addGlobalConfig(template.getCode() + "\n", globalConfigs));
    }

    /**
     * Inject global variables into templates
     *
     * @param templates     Multiple templates
     * @param globalConfigs Global variable
     */
    public static void addGlobalConfig(Collection<Template> templates, Collection<GlobalConfig> globalConfigs) {
        if (CollectionUtil.isEmpty(templates)) {
            return;
        }
        templates.forEach(template -> addGlobalConfig(template, globalConfigs));
    }

    /**
     * Inject global variables into templates
     *
     * @param templates Multiple templates
     */
    public static void addGlobalConfig(Collection<Template> templates) {
        addGlobalConfig(templates, CurrGroupUtils.getCurrGlobalConfigGroup().getElementList());
    }

    /**
     * Inject global variables into templates
     *
     * @param template Single template
     */
    public static void addGlobalConfig(Template template) {
        if (template != null) {
            addGlobalConfig(Collections.singleton(template));
        }
    }
}
