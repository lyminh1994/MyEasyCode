package com.sjhy.plugin.entity;

import lombok.Data;

/**
 * Callback entity class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
public class Callback {
    /**
     * File name
     */
    private String fileName;
    /**
     * Save route
     */
    private String savePath;
    /**
     * Whether to reformat the code
     */
    private Boolean reformat;
    /**
     * Whether to write files, some modules do not need to write files. For example debug.json template
     */
    private Boolean writeFile;
}
