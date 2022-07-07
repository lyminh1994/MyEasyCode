package com.sjhy.plugin.tool;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**
 * Velocity tool class, mainly used for code generation
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class VelocityUtils {

    /**
     * Velocity configuration
     */
    private static final Properties INIT_PROP;

    static {
        // Set initial configuration
        INIT_PROP = new Properties();
        // Fixed the issue that the velocity logging of some users did not have access to the velocity.log file
        INIT_PROP.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        INIT_PROP.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
    }

    /**
     * Forbid creation of instance objects
     */
    private VelocityUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Render template
     *
     * @param template Template string
     * @param map      Parameter set
     * @return Render result
     */
    public static String generate(String template, Map<String, Object> map) {
        // Create a new instance each time, preventing velocity from caching macro definitions
        VelocityEngine velocityEngine = new VelocityEngine(INIT_PROP);
        // Create context object
        VelocityContext velocityContext = new VelocityContext();
        if (map != null) {
            map.forEach(velocityContext::put);
        }
        StringWriter stringWriter = new StringWriter();
        try {
            // Generate code
            velocityEngine.evaluate(velocityContext, stringWriter, "Velocity Code Generate", template);
        } catch (Exception e) {
            // Catch all exceptions and return them directly for writing to templates
            StringBuilder builder = new StringBuilder("When generating code, the template has the following syntax error：\n");
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
            return builder.toString().replace("\r", "");
        }
        String code = stringWriter.toString();
        // Clear leading spaces
        StringBuilder sb = new StringBuilder(code);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
            sb.deleteCharAt(0);
        }
        // Return result
        return sb.toString();
    }
}
