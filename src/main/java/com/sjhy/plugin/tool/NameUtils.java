package com.sjhy.plugin.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Naming tool class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class NameUtils {
    private volatile static NameUtils nameUtils;

    /**
     * Singleton pattern
     */
    public static NameUtils getInstance() {
        if (nameUtils == null) {
            synchronized (NameUtils.class) {
                if (nameUtils == null) {
                    nameUtils = new NameUtils();
                }
            }
        }
        return nameUtils;
    }

    /**
     * Private constructor
     */
    NameUtils() {
    }

    /**
     * Convert CamelCase to Regular Matching Rules
     */
    private static final Pattern TO_HUMP_PATTERN = Pattern.compile("[-_]([a-z0-9])");
    private static final Pattern TO_LINE_PATTERN = Pattern.compile("[A-Z]+");

    /**
     * Capitalization method
     *
     * @param name Name
     * @return Result
     */
    public String firstUpperCase(String name) {
        return StringUtils.capitalize(name);
    }

    /**
     * First letter lowercase method
     *
     * @param name Name
     * @return Result
     */
    public String firstLowerCase(String name) {
        return StringUtils.uncapitalize(name);
    }

    /**
     * CamelCase to underscore, all lowercase
     *
     * @param str Camel case string
     * @return Underscore string
     */
    public String hump2Underline(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        Matcher matcher = TO_LINE_PATTERN.matcher(str);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (matcher.start() > 0) {
                matcher.appendReplacement(buffer, "_" + matcher.group(0).toLowerCase());
            } else {
                matcher.appendReplacement(buffer, matcher.group(0).toLowerCase());
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Get class name by java full name
     *
     * @param fullName Full name
     * @return Class name
     */
    public String getClsNameByFullName(String fullName) {
        int genericIdx = fullName.indexOf('<');
        if (genericIdx == -1) {
            return fullName.substring(fullName.lastIndexOf('.') + 1);
        }
        String className = fullName.substring(0, genericIdx);
        return fullName.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * Get class name by java full name
     *
     * @param fullName Full name
     * @return Class name
     */
    public String getClsFullNameRemoveGeneric(String fullName) {
        int genericIdx = fullName.indexOf('<');
        if (genericIdx == -1) {
            return fullName;
        }
        return fullName.substring(0, genericIdx);
    }

    /**
     * Underscore and horizontal line name to camel case name (attribute name)
     *
     * @param name Name
     * @return Result
     */
    public String getJavaName(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        // Force all lowercase
        name = name.toLowerCase();
        Matcher matcher = TO_HUMP_PATTERN.matcher(name.toLowerCase());
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Underscore and horizontal line name turn camel case name (class name)
     *
     * @param name Name
     * @return Result
     */
    public String getClassName(String name) {
        return firstUpperCase(getJavaName(name));
    }

    /**
     * Arbitrary object merge utility class
     *
     * @param objects Any object
     * @return Merged string result
     */
    public String append(Object... objects) {

        if (objects == null || objects.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Object s : objects) {
            if (s != null) {
                builder.append(s);
            }
        }
        return builder.toString();
    }
}
