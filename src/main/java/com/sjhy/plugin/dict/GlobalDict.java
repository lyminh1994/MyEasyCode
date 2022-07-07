package com.sjhy.plugin.dict;

/**
 * Global dictionary
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/07 11:41
 */
public interface GlobalDict {
    /**
     * Tips
     */
    String TITLE_INFO = "EasyCode Title Info";
    /**
     * Version number
     */
    String VERSION = "1.2.6";
    /**
     * Author name
     */
    String AUTHOR = "";
    /**
     * Default group name
     */
    String DEFAULT_GROUP_NAME = "Default";
    /**
     * List of default Java types
     */
    String[] DEFAULT_JAVA_TYPE_LIST = new String[]{
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.util.Boolean",
            "java.util.Date",
            "java.time.LocalDateTime",
            "java.time.LocalDate",
            "java.time.LocalTime",
            "java.lang.Short",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Character",
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.String[]",
            "java.util.List",
            "java.util.Set",
            "java.util.Map",
    };
}
