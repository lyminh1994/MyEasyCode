package com.sjhy.plugin.entity;

import com.sjhy.plugin.factory.AbstractItemFactory;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ReflectionUtils;

import java.util.List;

/**
 * Abstract grouping class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public interface AbstractGroup<T, E extends AbstractItem<E>> {
    /**
     * Get group name
     *
     * @return Group Name
     */
    String getName();

    /**
     * Set group name
     *
     * @param name Group Name
     */
    void setName(String name);

    /**
     * Get element collection
     *
     * @return Collection of elements
     */
    List<E> getElementList();

    /**
     * Set element collection
     *
     * @param elementList Collection of elements
     */
    void setElementList(List<E> elementList);

    /**
     * Default child element
     *
     * @return {@link E}
     */
    @SuppressWarnings("unchecked")
    default E defaultChild() {
        Class<E> cls = (Class<E>) ReflectionUtils.getGenericClass(this, 1);
        return AbstractItemFactory.createDefaultVal(cls);
    }

    /**
     * Clone object
     *
     * @return {@link T}
     */
    @SuppressWarnings("unchecked")
    default T cloneObj() {
        return (T) CloneUtils.cloneByJson(this);
    }
}
