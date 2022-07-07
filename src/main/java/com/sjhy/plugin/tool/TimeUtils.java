package com.sjhy.plugin.tool;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Time tool
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/19 13:16
 */
public class TimeUtils {
    private static volatile TimeUtils timeUtils;

    /**
     * Singleton pattern
     *
     * @return Instance object
     */
    public static TimeUtils getInstance() {
        if (timeUtils == null) {
            synchronized (TimeUtils.class) {
                if (timeUtils == null) {
                    timeUtils = new TimeUtils();
                }
            }
        }
        return timeUtils;
    }

    private TimeUtils() {

    }

    /**
     * Get the time string in the specified format
     *
     * @param pattern Format
     * @return Time string
     */
    public String currTime(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    /**
     * Get the time string in the default format (yyyy-MM-dd HH:mm:ss）
     *
     * @return Time string
     */
    public String currTime() {
        return currTime("yyyy-MM-dd HH:mm:ss");
    }
}
