package com.sjhy.plugin.entity;

/**
 * Abstract editable element
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/11 13:45
 */
public interface AbstractEditorItem<T extends AbstractItem> extends AbstractItem<T> {
    /**
     * Change file name
     *
     * @param name File name
     */
    void changeFileName(String name);

    /**
     * Get file name
     *
     * @return {@link String}
     */
    String fileName();

    /**
     * Change file content
     *
     * @param content Content
     */
    void changeFileContent(String content);

    /**
     * Get file content
     *
     * @return {@link String}
     */
    String fileContent();
}
