package com.sjhy.plugin.tool;

import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.ColumnConfigGroup;
import com.sjhy.plugin.entity.GlobalConfigGroup;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.service.SettingsStorageService;

/**
 * Current group configuration acquisition tool
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/01 16:51
 */
public final class CurrGroupUtils {
    /**
     * Disable constructor
     */
    private CurrGroupUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the current template group object
     *
     * @return Template group object
     */
    public static TemplateGroup getCurrTemplateGroup() {
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        String groupName = settingsStorage.getCurrTemplateGroupName();
        return settingsStorage.getTemplateGroupMap().get(groupName);
    }


    /**
     * Get the current global configuration group object
     *
     * @return Global configuration group object
     */
    public static GlobalConfigGroup getCurrGlobalConfigGroup() {
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        String groupName = settingsStorage.getCurrGlobalConfigGroupName();
        return settingsStorage.getGlobalConfigGroupMap().get(groupName);
    }


    /**
     * Get the current type map group object
     *
     * @return Type map group object
     */
    public static TypeMapperGroup getCurrTypeMapperGroup() {
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        String groupName = settingsStorage.getCurrTypeMapperGroupName();
        return settingsStorage.getTypeMapperGroupMap().get(groupName);
    }

    /**
     * Get the current column configuration group object
     *
     * @return Column Configuration Group Object
     */
    public static ColumnConfigGroup getCurrColumnConfigGroup() {
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        String groupName = settingsStorage.getCurrColumnConfigGroupName();
        return settingsStorage.getColumnConfigGroupMap().get(groupName);
    }

}
