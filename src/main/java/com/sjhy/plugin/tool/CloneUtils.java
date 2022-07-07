package com.sjhy.plugin.tool;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * Clone the tool class, the implementation principle is realized by JSON serialization and deserialization
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@SuppressWarnings("unchecked")
public final class CloneUtils {
    /**
     * Disable constructor
     */
    private CloneUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Clone through JSON serialization, and ignore objects by default
     *
     * @param entity        Instance object
     * @param typeReference Return type
     * @return Cloned entity object
     */
    public static <E, T extends E> E cloneByJson(E entity, TypeReference<T> typeReference) {
        return cloneByJson(entity, typeReference, false);
    }

    /**
     * Clone via JSON serialization
     *
     * @param entity Instance object
     * @return Cloned entity object
     */
    public static <E> E cloneByJson(E entity) {
        return cloneByJson(entity, false);
    }

    /**
     * Clone via JSON serialization
     *
     * @param entity Instance object
     * @param copy   Whether to copy ignored properties
     * @return Cloned entity object
     */
    public static <E> E cloneByJson(E entity, boolean copy) {
        return cloneByJson(entity, null, copy);
    }

    /**
     * Clone via JSON serialization
     *
     * @param entity        Instance object
     * @param copy          Whether to copy ignored properties
     * @param typeReference Return type
     * @return Cloned entity object
     */
    public static <E, T extends E> E cloneByJson(E entity, TypeReference<T> typeReference, boolean copy) {
        if (entity == null) {
            return null;
        }
        // Serialize
        String json = JSON.toJson(entity);
        // Deserialize
        E result;
        if (typeReference == null) {
            result = (E) JSON.parse(json, entity.getClass());
        } else {
            result = JSON.parse(json, typeReference);
        }
        // Copy ignored properties
        if (copy) {
            copyIgnoreProp(entity, result);
            // Do special processing for TableInfo objects
            if (entity instanceof TableInfo) {
                handlerTableInfo((TableInfo) entity, (TableInfo) result);
            }
        }
        return result;
    }

    /**
     * Do special treatment for TableInfo
     *
     * @param oldEntity Old entity object
     * @param newEntity New entity object
     */
    private static void handlerTableInfo(TableInfo oldEntity, TableInfo newEntity) {
        List<ColumnInfo> oldColumnInfoList = oldEntity.getFullColumn();
        List<ColumnInfo> newColumnInfoList = newEntity.getFullColumn();
        if (CollectionUtil.isEmpty(oldColumnInfoList) || CollectionUtil.isEmpty(newColumnInfoList)) {
            return;
        }
        // To process
        for (ColumnInfo oldColumnInfo : oldColumnInfoList) {
            for (ColumnInfo newColumnInfo : newColumnInfoList) {
                // Ignore replication for the same column information
                if (Objects.equals(oldColumnInfo.getName(), newColumnInfo.getName())) {
                    copyIgnoreProp(oldColumnInfo, newColumnInfo);
                    break;
                }
            }
        }
    }

    /**
     * Copy properties
     *
     * @param oldEntity On the entity
     * @param newEntity New instance
     */
    private static void copyIgnoreProp(Object oldEntity, Object newEntity) {
        // The types are different and return directly
        if (!Objects.equals(oldEntity.getClass(), newEntity.getClass())) {
            return;
        }
        // Get all fields
        List<Field> fieldList = ReflectionUtil.collectFields(oldEntity.getClass());
        if (CollectionUtil.isEmpty(fieldList)) {
            return;
        }
        fieldList.forEach(field -> {
            if (field.getAnnotation(JsonIgnore.class) != null) {
                // Set allow access
                field.setAccessible(true);
                // Copy field
                try {
                    field.set(newEntity, field.get(oldEntity));
                } catch (IllegalAccessException e) {
                    ExceptionUtil.rethrow(e);
                }
            }
        });
    }
}
