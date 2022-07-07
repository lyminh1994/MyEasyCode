package com.sjhy.plugin.factory;

import com.sjhy.plugin.entity.AbstractItem;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstract project factory
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/11 10:44
 */
public class AbstractItemFactory {

    private AbstractItemFactory() {
    }

    public static <T extends AbstractItem<T>> T createDefaultVal(Class<T> cls) {
        try {
            T instance = cls.getDeclaredConstructor().newInstance();
            return instance.defaultVal();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to build example", e);
        }
    }

}
