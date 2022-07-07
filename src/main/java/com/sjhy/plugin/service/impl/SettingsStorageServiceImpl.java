package com.sjhy.plugin.service.impl;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.SettingsStorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Set up storage service implementation
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/07 11:32
 */
@State(name = "EasyCodeSetting", storages = @Storage("easy-code-setting.xml"))
public class SettingsStorageServiceImpl implements SettingsStorageService {

    private SettingsStorageDTO settingsStorage = SettingsStorageDTO.defaultVal();

    /**
     * Get configuration
     *
     * @return Configuration object
     */
    @Nullable
    @Override
    public SettingsStorageDTO getState() {
        return settingsStorage;
    }

    /**
     * Load configuration
     *
     * @param state Configuration object
     */
    @Override
    public void loadState(@NotNull SettingsStorageDTO state) {
        // Fill in the default values after loading the configuration to avoid the problem of incomplete configuration information caused by version upgrades
        state.fillDefaultVal();
        this.settingsStorage = state;
    }
}
