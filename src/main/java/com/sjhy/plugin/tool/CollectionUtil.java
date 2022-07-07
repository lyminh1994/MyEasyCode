package com.sjhy.plugin.tool;

import java.util.Collection;
import java.util.Map;

/**
 * Collection tool class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/25 10:24
 */
public class CollectionUtil {

    private CollectionUtil() {
    }

    /**
     * Check if a collection is empty
     *
     * @param collection Collection object
     * @return Is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Check if map is empty
     *
     * @param map Map object
     * @return Is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
