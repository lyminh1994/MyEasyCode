package com.sjhy.plugin.entity;

import com.sjhy.plugin.tool.CloneUtils;

/**
 * Abstract item
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/11 09:47
 */
public interface AbstractItem<T extends AbstractItem> {
    /**
     * Defaults
     *
     * @return {@link T}
     */
    T defaultVal();

    /**
     * Clone object
     *
     * @return Cloning results
     */
    @SuppressWarnings("unchecked")
    default T cloneObj() {
        return (T) CloneUtils.cloneByJson(this);
    }
}
