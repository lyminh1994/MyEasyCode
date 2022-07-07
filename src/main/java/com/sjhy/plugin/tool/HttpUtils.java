package com.sjhy.plugin.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dict.GlobalDict;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Http tool class, add methods as needed
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/03 14:59
 */
public final class HttpUtils {
    /**
     * User Equipment Identification
     */
    private static final String USER_AGENT = "EasyCode";
    /**
     * Content type tag
     */
    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
    /**
     * Server address
     */
    private static final String HOST_URL = "http://www.shujuhaiyang.com/easyCode";
    /**
     * Http client
     */
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();

    /**
     * Request timeout setting (10 seconds)
     */
    private static final int TIMEOUT = 10 * 1000;

    /**
     * Status code
     */
    private static final String STATE_CODE = "code";

    /**
     * Private constructor
     */
    private HttpUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get request
     *
     * @param uri Request address
     * @return Return request result
     */
    public static String get(String uri) {
        HttpGet httpGet = new HttpGet(HOST_URL + uri);
        httpGet.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
        httpGet.setConfig(getDefaultConfig());
        return handlerRequest(httpGet);
    }

    /**
     * Post json request
     *
     * @param uri   Address
     * @param param Parameter
     * @return Request return result
     */
    public static String postJson(String uri, Object param) {
        HttpPost httpPost = new HttpPost(HOST_URL + uri);
        httpPost.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
        httpPost.setConfig(getDefaultConfig());
        httpPost.setEntity(new StringEntity(JSON.toJson(param), "utf-8" ));
        return handlerRequest(httpPost);
    }

    private static RequestConfig getDefaultConfig() {
        return RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).build();
    }

    /**
     * Unified processing of requests
     *
     * @param request Request object
     * @return Response string
     */
    private static String handlerRequest(HttpUriRequest request) {
        try {
            CloseableHttpResponse response = HTTP_CLIENT.execute(request);
            String body = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Messages.showWarningDialog("Error connecting to server！", GlobalDict.TITLE_INFO);
                return null;
            }
            HttpClientUtils.closeQuietly(response);
            // Parse JSON data
            ObjectMapper objectMapper = JSON.getInstance();
            JsonNode jsonNode = objectMapper.readTree(body);
            if (jsonNode.get(STATE_CODE).asInt() == 0) {
                JsonNode data = jsonNode.get("data");
                if (data instanceof TextNode) {
                    return data.asText();
                }
                return data.toString();
            }
            // Get error message
            String msg = jsonNode.get("msg").asText();
            Messages.showWarningDialog(msg, GlobalDict.TITLE_INFO);
        } catch (IOException e) {
            Messages.showWarningDialog("Unable to connect to server!", GlobalDict.TITLE_INFO);
            ExceptionUtil.rethrow(e);
        }
        return null;
    }
}
