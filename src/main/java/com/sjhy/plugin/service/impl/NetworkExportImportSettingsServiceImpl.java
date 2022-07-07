package com.sjhy.plugin.service.impl;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.util.TextRange;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.ExportImportSettingsService;
import com.sjhy.plugin.tool.HttpUtils;
import com.sjhy.plugin.tool.JSON;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.tool.StringUtils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network export import settings service implementation
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/12 14:37
 */
public class NetworkExportImportSettingsServiceImpl implements ExportImportSettingsService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-z0-9A-Z]{20,}+");

    /**
     * Export settings
     *
     * @param settingsStorage Settings to export
     */
    @Override
    public void exportConfig(SettingsStorageDTO settingsStorage) {
        // Upload data
        String result = HttpUtils.postJson("/template", settingsStorage);
        if (result != null) {
            // Extract token value using regular
            String token = "error";
            Matcher matcher = TOKEN_PATTERN.matcher(result);
            if (matcher.find()) {
                token = matcher.group();
            }
            // Show token
            try {
                Method method = Messages.class.getMethod("showInputDialog", Project.class, String.class, String.class, Icon.class, String.class, InputValidator.class, TextRange.class, String.class);
                method.invoke(null, ProjectUtils.getCurrProject(), result, GlobalDict.TITLE_INFO, AllIcons.General.InformationDialog, token, new NonEmptyInputValidator(), null, "Easy Code official website address：<a href='http://www.shujuhaiyang.com'>www.shujuhaiyang.com</a>");
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Compatible with older versions
                Messages.showInputDialog(ProjectUtils.getCurrProject(), result, GlobalDict.TITLE_INFO, AllIcons.General.InformationDialog, token, new NonEmptyInputValidator(), null);
            }
        }
    }

    /**
     * Import settings
     *
     * @return Setup information
     */
    @Override
    public SettingsStorageDTO importConfig() {
        String token = Messages.showInputDialog("Token:", GlobalDict.TITLE_INFO, AllIcons.General.Tip, "", new InputValidator() {
            @Override
            public boolean checkInput(String inputString) {
                return !StringUtils.isEmpty(inputString);
            }

            @Override
            public boolean canClose(String inputString) {
                return this.checkInput(inputString);
            }
        });
        if (token == null) {
            return null;
        }
        String result = HttpUtils.get(String.format("/template?token=%s", token));
        if (result == null) {
            return null;
        }
        // Analytical data
        return JSON.parse(result, SettingsStorageDTO.class);
    }
}
