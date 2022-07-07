package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.SettingsStorageService;
import org.jetbrains.annotations.Nullable;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/07 19:42
 */
public interface BaseSettings extends Configurable {
    /**
     * Help prompt information
     *
     * @return Tips
     */
    @Nullable
    @Override
    default String getHelpTopic() {
        return getDisplayName();
    }

    /**
     * Reset settings
     */
    @Override
    default void reset() {
        loadSettingsStore();
    }

    /**
     * Get setting information
     *
     * @return Get setting information
     */
    default SettingsStorageDTO getSettingsStorage() {
        return SettingsStorageService.getSettingsStorage();
    }

    /**
     * Load configuration information
     */
    default void loadSettingsStore() {
        this.loadSettingsStore(getSettingsStorage());
    }

    /**
     * Load configuration information
     *
     * @param settingsStorage configuration information
     */
    void loadSettingsStore(SettingsStorageDTO settingsStorage);

}
