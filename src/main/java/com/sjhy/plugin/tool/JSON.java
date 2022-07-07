package com.sjhy.plugin.tool;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Json tools
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/08/14 10:37
 */
public class JSON {

    private JSON() {
    }

    private static final ObjectMapper INSTANCE;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    static {
        INSTANCE = new ObjectMapper();
        // Disable serialization of dates into timestamps
        INSTANCE.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Disable error when forbidden attribute does not exist
        INSTANCE.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // Allow strings to be converted to arrays
        INSTANCE.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        // Read to unknown enum value converted to null
        INSTANCE.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        // Disable scientific notation
        INSTANCE.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        INSTANCE.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        // Serialization ignores null values
        INSTANCE.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Configure time format
        INSTANCE.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
    }

    public static ObjectMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Convert json string to java object
     *
     * @param json Json string
     * @param cls  Java object type
     * @param <T>  Object type
     * @return Object
     */
    public static <T> T parse(String json, Class<T> cls) {
        try {
            return INSTANCE.readValue(json, cls);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert json string to java object
     *
     * @param json Json string
     * @param type Java object type
     * @param <T>  Object type
     * @return Object
     */
    public static <T> T parse(String json, TypeReference<T> type) {
        try {
            return INSTANCE.readValue(json, type);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert json object to json string
     *
     * @param obj Object
     * @return Json string
     */
    public static String toJson(Object obj) {
        try {
            return INSTANCE.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert json object to json string
     *
     * @param obj Object
     * @return Json string
     */
    public static String toJsonByFormat(Object obj) {
        try {
            return INSTANCE.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JsonNode readTree(Object obj) {
        try {
            if (obj instanceof String) {
                return INSTANCE.readTree((String) obj);
            } else if (obj instanceof byte[]) {
                return INSTANCE.readTree((byte[]) obj);
            } else if (obj instanceof InputStream) {
                return INSTANCE.readTree((InputStream) obj);
            } else if (obj instanceof URL) {
                return INSTANCE.readTree((URL) obj);
            } else if (obj instanceof File) {
                return INSTANCE.readTree((File) obj);
            }
            // Other objects, convert to string and then convert to JsonNode
            return INSTANCE.readTree(toJson(obj));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
