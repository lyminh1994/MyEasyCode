package com.sjhy.plugin.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.DbUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.util.ReflectionUtil;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.GenerateOptions;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.Callback;
import com.sjhy.plugin.entity.SaveFile;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.service.CodeGenerateService;
import com.sjhy.plugin.service.SettingsStorageService;
import com.sjhy.plugin.service.TableInfoSettingsService;
import com.sjhy.plugin.tool.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/02 12:50
 */
public class CodeGenerateServiceImpl implements CodeGenerateService {
    /**
     * Project object
     */
    private Project project;
    /**
     * Model management
     */
    private ModuleManager moduleManager;
    /**
     * Table Information Services
     */
    private TableInfoSettingsService tableInfoService;
    /**
     * Cache data tool
     */
    private CacheDataUtils cacheDataUtils;
    /**
     * Package prefix to filter when importing packages
     */
    private static final String FILTER_PACKAGE_NAME = "java.lang";

    public CodeGenerateServiceImpl(Project project) {
        this.project = project;
        this.moduleManager = ModuleManager.getInstance(project);
        this.tableInfoService = TableInfoSettingsService.getInstance();
        this.cacheDataUtils = CacheDataUtils.getInstance();
    }

    /**
     * Generate
     *
     * @param templates       Template
     * @param generateOptions Build options
     */
    @Override
    public void generate(Collection<Template> templates, GenerateOptions generateOptions) {
        // Get selected table information
        TableInfo selectedTableInfo;
        List<TableInfo> tableInfoList;
        if (Boolean.TRUE.equals(generateOptions.getEntityModel())) {
            selectedTableInfo = tableInfoService.getTableInfo(cacheDataUtils.getSelectPsiClass());
            tableInfoList = cacheDataUtils.getPsiClassList().stream().map(item -> tableInfoService.getTableInfo(item)).collect(Collectors.toList());
        } else {
            selectedTableInfo = tableInfoService.getTableInfo(cacheDataUtils.getSelectDbTable());
            tableInfoList = cacheDataUtils.getDbTableList().stream().map(item -> tableInfoService.getTableInfo(item)).collect(Collectors.toList());
        }
        // Verify that the save path of the selected table is correct
        if (StringUtils.isEmpty(selectedTableInfo.getSavePath())) {
            if (selectedTableInfo.getObj() != null) {
                Messages.showInfoMessage(selectedTableInfo.getObj().getName() + " table configuration information is incorrect, please try again", GlobalDict.TITLE_INFO);
            } else if (selectedTableInfo.getPsiClassObj() != null) {
                PsiClass psiClassObj = (PsiClass) selectedTableInfo.getPsiClassObj();
                Messages.showInfoMessage(psiClassObj.getName() + " the class configuration information is incorrect, please try again", GlobalDict.TITLE_INFO);
            } else {
                Messages.showInfoMessage("The configuration information is incorrect, please try again", GlobalDict.TITLE_INFO);
            }
            return;
        }
        // Override un-configured tables
        TableInfo finalSelectedTableInfo = selectedTableInfo;
        tableInfoList.forEach(tableInfo -> {
            if (StringUtils.isEmpty(tableInfo.getSavePath())) {
                tableInfo.setSaveModelName(finalSelectedTableInfo.getSaveModelName());
                tableInfo.setSavePackageName(finalSelectedTableInfo.getSavePackageName());
                tableInfo.setSavePath(finalSelectedTableInfo.getSavePath());
                tableInfo.setPreName(finalSelectedTableInfo.getPreName());
                tableInfoService.saveTableInfo(tableInfo);
            }
        });
        // If you use a unified configuration, directly overwrite all
        if (Boolean.TRUE.equals(generateOptions.getUnifiedConfig())) {
            tableInfoList.forEach(tableInfo -> {
                tableInfo.setSaveModelName(finalSelectedTableInfo.getSaveModelName());
                tableInfo.setSavePackageName(finalSelectedTableInfo.getSavePackageName());
                tableInfo.setSavePath(finalSelectedTableInfo.getSavePath());
                tableInfo.setPreName(finalSelectedTableInfo.getPreName());
            });
        }

        // Generate code
        generate(templates, tableInfoList, generateOptions, null);
    }

    /**
     * Generate code and automatically save it to the corresponding location
     *
     * @param templates       Template
     * @param tableInfoList   Table info object
     * @param generateOptions Build configuration
     * @param otherParam      Other parameters
     */
    public void generate(Collection<Template> templates, Collection<TableInfo> tableInfoList, GenerateOptions generateOptions, Map<String, Object> otherParam) {
        if (CollectionUtil.isEmpty(templates) || CollectionUtil.isEmpty(tableInfoList)) {
            return;
        }
        // Process templates and inject global variables (clone a copy to prevent tampering)
        templates = CloneUtils.cloneByJson(templates, new TypeReference<ArrayList<Template>>() {
        });
        TemplateUtils.addGlobalConfig(templates);
        // Generate code
        for (TableInfo tableInfo : tableInfoList) {
            // Remove prefix from table name
            if (!StringUtils.isEmpty(tableInfo.getPreName()) && tableInfo.getObj().getName().startsWith(tableInfo.getPreName())) {
                String newName = tableInfo.getObj().getName().substring(tableInfo.getPreName().length());
                tableInfo.setName(NameUtils.getInstance().getClassName(newName));
            }
            // Build parameters
            Map<String, Object> param = getDefaultParam();
            // Other parameters
            if (otherParam != null) {
                param.putAll(otherParam);
            }
            // All table info objects
            param.put("tableInfoList", tableInfoList);
            // Table info object
            param.put("tableInfo", tableInfo);
            // Set model path and import package list
            setModulePathAndImportList(param, tableInfo);
            // Setting up additional code generation services
            param.put("generateService", new ExtraCodeGenerateUtils(this, tableInfo, generateOptions));
            for (Template template : templates) {
                Callback callback = new Callback();
                callback.setWriteFile(true);
                callback.setReformat(generateOptions.getReFormat());
                // Default name
                callback.setFileName(tableInfo.getName() + "Default.java");
                // Default path
                callback.setSavePath(tableInfo.getSavePath());
                // Set callback object
                param.put("callback", callback);
                // Start generating
                String code = VelocityUtils.generate(template.getCode(), param);
                // Set a default save path and default filename
                String path = callback.getSavePath();
                path = path.replace("\\", "/");
                // Handling relative paths
                if (path.startsWith(".")) {
                    path = project.getBasePath() + path.substring(1);
                }
                callback.setSavePath(path);
                new SaveFile(project, code, callback, generateOptions).write();
            }
        }
    }

    /**
     * Generate code
     *
     * @param template  Template
     * @param tableInfo Table info object
     * @return Generated code
     */
    @Override
    public String generate(Template template, TableInfo tableInfo) {
        // Get default parameters
        Map<String, Object> param = getDefaultParam();
        // Table information object, clone, prevent tampering
        param.put("tableInfo", tableInfo);
        // Set model path and import package list
        setModulePathAndImportList(param, tableInfo);
        // Process templates, inject global variables
        TemplateUtils.addGlobalConfig(template);
        return VelocityUtils.generate(template.getCode(), param);
    }

    /**
     * Set model path and import package list
     *
     * @param param     Parameter
     * @param tableInfo Table info object
     */
    private void setModulePathAndImportList(Map<String, Object> param, TableInfo tableInfo) {
        Module module = null;
        if (!StringUtils.isEmpty(tableInfo.getSaveModelName())) {
            module = this.moduleManager.findModuleByName(tableInfo.getSaveModelName());
        }
        if (module != null) {
            // Set modulePath
            param.put("modulePath", ModuleUtils.getModuleDir(module).getPath());
        }
        // Set packages to import
        param.put("importList", getImportList(tableInfo));
    }

    /**
     * Get default parameters
     *
     * @return Parameter
     */
    private Map<String, Object> getDefaultParam() {
        // System settings
        SettingsStorageDTO settings = SettingsStorageService.getSettingsStorage();
        Map<String, Object> param = new HashMap<>(20);
        // Author
        param.put("author", settings.getAuthor());
        //Tools
        param.put("tool", GlobalTool.getInstance());
        param.put("time", TimeUtils.getInstance());
        // Project path
        param.put("projectPath", project.getBasePath());
        // DatabaseDatabase Tools
        param.put("dbUtil", ReflectionUtil.newInstance(DbUtil.class));
        param.put("dasUtil", ReflectionUtil.newInstance(DasUtil.class));
        return param;
    }

    /**
     * Get import list
     *
     * @param tableInfo Table info object
     * @return Import list
     */
    private Set<String> getImportList(TableInfo tableInfo) {
        // Create a self-sorted collection
        Set<String> result = new TreeSet<>();
        tableInfo.getFullColumn().forEach(columnInfo -> {
            if (!columnInfo.getType().startsWith(FILTER_PACKAGE_NAME)) {
                String type = NameUtils.getInstance().getClsFullNameRemoveGeneric(columnInfo.getType());
                result.add(type);
            }
        });
        return result;
    }
}
