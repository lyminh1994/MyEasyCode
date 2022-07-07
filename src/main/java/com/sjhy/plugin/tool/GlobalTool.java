package com.sjhy.plugin.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;
import com.sjhy.plugin.entity.DebugField;
import com.sjhy.plugin.entity.DebugMethod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Global tool class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/14 18:11
 */
@SuppressWarnings("unused")
public class GlobalTool extends NameUtils {
    private static volatile GlobalTool globalTool;

    /**
     * Private constructor
     */
    private GlobalTool() {
    }

    /**
     * Singleton pattern
     */
    public static GlobalTool getInstance() {
        if (globalTool == null) {
            synchronized (GlobalTool.class) {
                if (globalTool == null) {
                    globalTool = new GlobalTool();
                }
            }
        }
        return globalTool;
    }

    /**
     * Create a collection
     *
     * @param items Initial element
     * @return Collection object
     */
    public Set<?> newHashSet(Object... items) {
        return items == null ? new HashSet<>() : new HashSet<>(Arrays.asList(items));
    }

    /**
     * Create a list
     *
     * @param items Initial element
     * @return List object
     */
    public List<?> newArrayList(Object... items) {
        return items == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(items));
    }

    /**
     * Create an ordered Map
     *
     * @return Map object
     */
    public Map<?, ?> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * Create an unordered Map
     *
     * @return Map object
     */
    public Map<?, ?> newHashMap() {
        return new HashMap<>(16);
    }

    /**
     * Get fields, mandatory access like private properties
     *
     * @param obj       Object
     * @param fieldName Field name
     * @return Field value
     */
    public Object getField(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        Class<?> cls = obj.getClass();
        return ReflectionUtil.getField(cls, obj, Object.class, fieldName);
    }

    /**
     * No-return execution, used to eliminate the return value
     *
     * @param obj Receive execution return value
     */
    public void call(Object... obj) {
        // document why this method is empty
    }

    /**
     * Get all fields of a class
     *
     * @param cls Kind
     * @return All fields
     */
    private List<Field> getAllFieldByClass(Class<?> cls) {
        List<Field> result = new ArrayList<>();
        do {
            result.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        } while (!cls.equals(Object.class));
        return result;
    }

    /**
     * Mode object
     *
     * @param obj Object
     * @return Debug JSON result
     */
    public String debug(Object obj) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (obj == null) {
            result.put("title", "Debug object is null");
            return JSON.toJsonByFormat(result);
        }
        // Get class
        Class<?> cls = obj.getClass();
        result.put("title", String.format("调试：%s", cls.getName()));
        // Method list
        List<DebugMethod> debugMethodList = new ArrayList<>();
        // Method of exclusion
        List<String> filterMethodName = Arrays.asList("hashCode", "toString", "equals", "getClass", "clone", "notify", "notifyAll", "wait", "finalize");
        for (Method method : cls.getMethods()) {
            if (filterMethodName.contains(method.getName())) {
                continue;
            }
            DebugMethod debugMethod = new DebugMethod();
            String methodName = method.getName();
            debugMethod.setName(methodName);
            debugMethod.setDesc(method.toGenericString());
            // Call and get the value for the no-argument method starting with get and is.
            if ((methodName.startsWith("get") || methodName.startsWith("is"))) {
                if (method.getParameterCount() == 0) {
                    try {
                        Object val = method.invoke(obj);
                        if (val != null) {
                            debugMethod.setValue(val.toString());
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        ExceptionUtil.rethrow(e);
                    }
                }
            }
            // Add to list
            debugMethodList.add(debugMethod);
        }
        result.put("methodList", debugMethodList);
        // Add a split first
        result.put("----", "-----------------I am a gorgeous dividing line-----------------");
        // Field list
        List<Field> fieldList = getAllFieldByClass(cls);
        List<DebugField> debugFieldList = new ArrayList<>();
        fieldList.forEach(field -> {
            DebugField debugField = new DebugField();
            debugField.setName(field.getName());
            debugField.setType(field.getType());
            try {
                // Set allow method
                field.setAccessible(true);
                Object val = field.get(obj);
                if (val == null) {
                    debugField.setValue(null);
                } else {
                    debugField.setValue(val.toString());
                }
            } catch (IllegalAccessException e) {
                ExceptionUtil.rethrow(e);
            }
            debugFieldList.add(debugField);
        });
        result.put("fieldList", debugFieldList);
        return JSON.toJsonByFormat(result).replace("\r\n", "\n");
    }

    private static final long MAX = 100000000000000000L;

    /**
     * Generate serial numbers with length 18 digits, keeping the code beautiful
     *
     * @return Serialization
     */
    public String serial() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        // Sign generation
        if (random.nextFloat() > 0.5F) {
            builder.append("-");
        }
        // The first position cannot be 0
        builder.append(random.nextInt(9) + 1);
        // Generate remaining digits
        do {
            builder.append(random.nextInt(10));
        } while (builder.length() < 18);
        // Add end symbol
        builder.append("L");
        return builder.toString();
    }

    /**
     * Convert json to map
     *
     * @param json Json string
     * @return Map object
     */
    public Map parseJson(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyMap();
        }
        try {
            return JSON.parse(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Convert object to json string
     *
     * @param obj Object
     * @return Json string
     */
    public String toJson(Object obj) {
        return toJson(obj, false);
    }

    /**
     * Convert object to json string
     *
     * @param obj    Object
     * @param format Whether to format json
     * @return Json string
     */
    public String toJson(Object obj, Boolean format) {
        if (obj == null) {
            return null;
        }
        if (format == null) {
            format = false;
        }
        // Whether to format the output json
        if (format) {
            return JSON.toJsonByFormat(obj);
        } else {
            return JSON.toJson(obj);
        }
    }

    /**
     * Chinese and Chinese symbols regular expressions
     */
    public static final String CHINESE_REGEX = "[\u4e00-\u9fa5–—‘’“”…、。〈〉《》「」『』【】〔〕！（），．：；？]";

    /**
     * String to unicode encoding (by default, only characters matched by CHINESE_REGEX are converted)
     *
     * @param str String
     * @return Transcode string
     */
    public String toUnicode(String str) {
        return toUnicode(str, false);
    }

    /**
     * String to unicode encoding
     *
     * @param str      String
     * @param transAll True converts all characters, false converts only characters matched by CHINESE_REGEX
     * @return Transcode string
     */
    public String toUnicode(String str, Boolean transAll) {
        if (null == str) {
            return null;
        }
        if (str.length() <= 0) {
            return null;
        }
        if (null == transAll) {
            transAll = false;
        }

        StringBuilder sb = new StringBuilder();
        if (transAll) {
            for (char c : str.toCharArray()) {
                sb.append(String.format("\\u%04x", (int) c));
            }
        } else {
            for (char c : str.toCharArray()) {
                // Chinese range
                if (String.valueOf(c).matches(CHINESE_REGEX)) {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Remote call service
     *
     * @param name  Service name
     * @param param Request parameter
     * @return Result
     */
    public Object service(String name, Object... param) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        // Assembly parameters
        Map<String, Object> map = Collections.emptyMap();
        if (param != null && param.length > 0) {
            map = new LinkedHashMap<>(param.length);
            for (int i = 0; i < param.length; i++) {
                map.put("param" + i, param[0]);
            }
        }
        // Make a request
        String result = HttpUtils.postJson(String.format("/service?name=%s", name), map);
        if (result == null) {
            return null;
        }
        try {
            // Process result
            JsonNode jsonNode = JSON.readTree(result);
            String type = jsonNode.get("type").asText();
            JsonNode data = jsonNode.get("data");
            Class<?> cls = Class.forName(type);
            // String type
            if (String.class.equals(cls)) {
                return data.asText();
            }
            // Other types
            return JSON.parse(data.toString(), cls);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
