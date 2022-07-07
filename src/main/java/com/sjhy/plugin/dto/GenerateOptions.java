package com.sjhy.plugin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Build options
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/17 09:08
 */
@Data
@Builder
public class GenerateOptions {
    /**
     * Entity class pattern
     */
    private Boolean entityModel;
    /**
     * Unified configuration
     */
    private Boolean unifiedConfig;
    /**
     * Reformat code
     */
    private Boolean reFormat;
    /**
     * Prompt to select yes
     */
    private Boolean titleSure;
    /**
     * Prompt to choose no
     */
    private Boolean titleRefuse;
}
