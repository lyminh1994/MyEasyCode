package com.sjhy.plugin.tool;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Add a string tool class, in order to be compatible with various JB products, try not to use third-party toolkits
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/07 11:52
 */
@SuppressWarnings("WeakerAccess")
public class StringUtils {

    private StringUtils() {
    }

    /**
     * Initial letter processing method
     */
    private static final BiFunction<String, Function<Integer, Integer>, String> FIRST_CHAR_HANDLER_FUN = (str, firstCharFun) -> {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = firstCharFun.apply(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            // already capitalized
            return str;
        }

        // cannot be longer than the char array
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        // copy the first codepoint
        newCodePoints[outOffset++] = newCodePoint;
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = str.codePointAt(inOffset);
            // copy the remaining ones
            newCodePoints[outOffset++] = codepoint;
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    };

    /**
     * Judgment is an empty string
     *
     * @param cs String
     * @return Is empty
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Capitalization method
     *
     * @param str String
     * @return Capitalize results
     */
    public static String capitalize(final String str) {
        return FIRST_CHAR_HANDLER_FUN.apply(str, Character::toTitleCase);
    }

    /**
     * First letter lowercase method
     *
     * @param str String
     * @return Lowercase result
     */
    public static String uncapitalize(final String str) {
        return FIRST_CHAR_HANDLER_FUN.apply(str, Character::toLowerCase);
    }
}
