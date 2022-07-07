package com.sjhy.plugin.service;

import com.sjhy.plugin.dto.SettingsStorageDTO;

/**
 * Export Import Settings Service
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/11 17:24
 */
public interface ExportImportSettingsService {

    /**
     * Export settings
     *
     * @param settingsStorage Settings to export
     */
    void exportConfig(SettingsStorageDTO settingsStorage);

    /**
     * Import settings
     *
     * @return Setup information
     */
    SettingsStorageDTO importConfig();

}
