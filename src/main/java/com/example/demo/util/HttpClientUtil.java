package com.example.demo.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Title: HttpClientUtil</p>
 * <p>Description: </p>
 * <p>Company: sunline</p>
 * @author Kuangyu
 * @date 2019年3月12日
 * @version 1.0
 */
public class HttpClientUtil {

    private static final  Logger LOG  =  LoggerFactory.getLogger(HttpClientUtil.class);

    private static int defaultTimeout = 30000;

    /**
     *
     * <p>Title: getCloseableHttpClient</p>
     * <p>Description: 根据请求URL生成对应的CloseableHttpClient对象</p>
     * @param httpUrl 请求URL
     * @return
     * @throws MalformedURLException
     */
    private static CloseableHttpClient getCloseableHttpClient(String httpUrl) throws MalformedURLException {
        return getCloseableHttpClient(isHttps(httpUrl));
    }

    /**
     *
     * <p>Title: getCloseableHttpClient</p>
     * <p>Description: 根据 boolean值isHttps生成对应的 CloseableHttpClient</p>
     * @param isHttps 是否为HTTPS协议请求
     * @return
     */
    private static CloseableHttpClient getCloseableHttpClient(boolean isHttps) {
        if (isHttps) {
            // https协议请求
            SSLContext sslcontext;
            try {
                // 允许所有证书
                sslcontext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
                // 忽略域名校验
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslcontext,
                        NoopHostnameVerifier.INSTANCE);
                // NoopHostnameVerifier HttpClien since 4.4
                // SSLConnectionSocketFactory socketFactory = new
                // SSLConnectionSocketFactory(sslcontext,
                // SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                // SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER HttpClien 4.3
                return HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            } catch (Exception e) {
                LOG.error("build HttpClient failed", e);
            }
        } else {
            // http协议请求
            return HttpClients.custom().build();
        }
        return null;
    }

    /**
     *
     * <p>Title: getRequestConfig</p>
     * <p>Description: 获得RequestConfig对象，设置请求超时时间</p>
     * @param timeout 请求超时时间，单位 毫秒
     * @return
     */
    private static RequestConfig getRequestConfig(int timeout) {
        return RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
    }

    /**
     *
     * <p>Title: isHttps</p>
     * <p>Description: 判断请求URL是否为HTTPS协议</p>
     * @param httpUrl
     * @return
     * @throws MalformedURLException
     */
    private static boolean isHttps(String httpUrl) throws MalformedURLException {
        URL url = new URL(httpUrl);
        return "https".equals(url.getProtocol());
    }

    /**
     *
     * <p>Title: doRequest</p>
     * <p>Description: 执行HTTP/HTTPS请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param httpUriRequest HttpUriRequest 对象
     * @return
     */
    private static String doRequest(String httpUrl, HttpUriRequest httpUriRequest) {
        // 响应结果
        String responseContent = null;
        if (httpUrl == null || httpUriRequest == null) {
            return responseContent;
        }
        // 可关闭的HttpClient对象
        CloseableHttpClient httpClient = null;
        try {

            httpClient = getCloseableHttpClient(httpUrl);
            if (httpClient == null) {
                return responseContent;
            }
            // 执行请求 ，获取响应HttpResponse
            HttpResponse httpResponse = httpClient.execute(httpUriRequest);
            // 获取响应实体HttpEntity
            HttpEntity entity = httpResponse.getEntity();
            if (null != entity) {
                // 转换为字符串
                responseContent = EntityUtils.toString(entity, "UTF-8");
                // Consume response content
                EntityUtils.consume(entity);
            }
            return responseContent;
        } catch (Exception e) {
            LOG.error("httpClient execute failed", e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    LOG.debug("httpClient close failed");
                }
            }
        }
        return responseContent;
    }

    /**
     *
     * <p>Title: getHttpUriRequest</p>
     * <p>Description: 根据是否带有参数，获得对应的HttpUriRequest对象</p>
     * @param httpUrl 请求URL
     * @param header 请求头
     * @param params 请求参数 字符串
     * @param timeout 超时时间
     * @return
     */
    private static HttpUriRequest getHttpUriRequest(String httpUrl, Map<String, String> header, String params,
            Integer timeout) {
        HttpRequestBase httpRequestBase = null;
        if (timeout == null || timeout < defaultTimeout) {
            // 为空则使用默认超时时间
            timeout = defaultTimeout;
        }
        if (params != null) {
            // 有参数则创建HttpPost对象
            HttpPost httpPost = new HttpPost(httpUrl);
            StringEntity stringEntity = new StringEntity(params, "UTF-8");
            // 设置参数
            httpPost.setEntity(stringEntity);
            httpRequestBase = httpPost;
        } else {
            // 无参数则创建HttpGet对象
            httpRequestBase = new HttpGet(httpUrl);
        }
        // 生成RequestConfig对象，设置请求超时时间
        RequestConfig requestConfig = getRequestConfig(timeout);
        setConfigAndHeader(header, httpRequestBase, requestConfig);
        return httpRequestBase;
    }

    /**
     * 设置请求头和其他配置信息
     * @param header
     * @param httpRequestBase
     * @param requestConfig
     */
    private static void setConfigAndHeader(Map<String, String> header, HttpRequestBase httpRequestBase,
            RequestConfig requestConfig) {
        if (httpRequestBase != null) {
            httpRequestBase.setConfig(requestConfig);
            if (header != null) {
                // 设置请求头信息
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpRequestBase.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     *
     * <p>Title: getHttpUriRequest</p>
     * <p>Description: 根据是否带有参数，获得对应的HttpUriRequest对象</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 map -- key=value
     * @param timeout 超时时间
     * @param header 请求头
     * @return
     */
    private static HttpUriRequest getHttpUriRequest(String httpUrl, Map<String, String> params, Integer timeout,
            Map<String, String> header) {
        HttpRequestBase httpRequestBase = null;
        if (timeout == null || timeout < defaultTimeout) {
            // 为空则使用默认超时时间
            timeout = defaultTimeout;
        }
        if (params != null) {
            // 有参数则创建HttpPost对象
            HttpPost httpPost = new HttpPost(httpUrl);
            // 构建POST请求的表单参数
            List<NameValuePair> formParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            try {
                // 设置参数
                httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOG.debug("httpPost setEntity failed", e);
            }
            httpRequestBase = httpPost;
        } else {
            // 无参数则创建HttpGet对象
            httpRequestBase = new HttpGet(httpUrl);
        }
        // 生成RequestConfig对象，设置请求超时时间
        RequestConfig requestConfig = getRequestConfig(timeout);
        setConfigAndHeader(header, httpRequestBase, requestConfig);
        return httpRequestBase;
    }

    /**
     *
     * <p>Title: doGet</p>
     * <p>Description: 执行get请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @return
     */
    public static String doGet(String httpUrl) {
        return doGet(httpUrl, null, null);
    }

    /**
     *
     * <p>Title: doGet</p>
     * <p>Description: 执行get请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param header 请求头
     * @return
     */
    public static String doGet(String httpUrl, Map<String, String> header) {
        return doGet(httpUrl, null, header);
    }

    /**
     *
     * <p>Title: doGet</p>
     * <p>Description: 执行get请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param timeout 超时时间
     * @return
     */
    public static String doGet(String httpUrl, Integer timeout) {
        return doGet(httpUrl, timeout, null);
    }

    /**
     *
     * <p>Title: doGet</p>
     * <p>Description: 执行get请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param timeout 超时时间
     * @param header 请求头
     * @return
     */
    public static String doGet(String httpUrl, Integer timeout, Map<String, String> header) {
        return doRequest(httpUrl, getHttpUriRequest(httpUrl, null, timeout, header));
    }

    /**
     *
     * <p>Title: doPost</p>
     * <p>Description: 执行post请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 map -- key=value
     * @return
     */
    public static String doPost(String httpUrl, Map<String, String> params) {
        return doPost(httpUrl, params, null, null);
    }

    /**
     *
     * <p>Title: doPost</p>
     * <p>Description: 执行post请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 map -- key=value
     * @param header 请求头
     * @return
     */
    public static String doPost(String httpUrl, Map<String, String> params, Map<String, String> header) {
        return doPost(httpUrl, params, null, header);
    }

    /**
     *
     * <p>Title: doPost</p>
     * <p>Description: 执行post请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 map -- key=value
     * @param timeout 超时时间
     * @return
     */
    public static String doPost(String httpUrl, Map<String, String> params, Integer timeout) {
        return doPost(httpUrl, params, timeout, null);
    }

    /**
     *
     * <p>Title: doPost</p>
     * <p>Description: 执行post请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 map -- key=value
     * @param timeout 超时时间
     * @param header 请求头
     * @return
     */
    public static String doPost(String httpUrl, Map<String, String> params, Integer timeout,
                                Map<String, String> header) {
        return doRequest(httpUrl, getHttpUriRequest(httpUrl, params, timeout, header));
    }

    /**
     *
     * <p>Title: doPost</p>
     * <p>Description: 执行post请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 字符串
     * @return
     */
    public static String doPost(String httpUrl, String params) {
        return doPost(httpUrl, params, null, null);
    }

    /**
     *
     * <p>Title: doPost</p>
     * <p>Description: 执行post请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 字符串
     * @param header 请求头
     * @return
     */
    public static String doPost(String httpUrl, String params, Map<String, String> header) {
        return doPost(httpUrl, params, null, header);
    }

    /**
     *
     * <p>Title: doPost</p>
     * <p>Description: 执行post请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 字符串
     * @param timeout 超时时间
     * @return
     */
    public static String doPost(String httpUrl, String params, Integer timeout) {
        return doPost(httpUrl, params, timeout, null);
    }

    /**
     *
     * <p>Title: doPost</p>
     * <p>Description: 执行post请求，获得响应结果</p>
     * @param httpUrl 请求URL
     * @param params 请求参数 字符串
     * @param timeout 超时时间
     * @param header 请求头
     * @return
     */
    public static String doPost(String httpUrl, String params, Integer timeout, Map<String, String> header) {
        return doRequest(httpUrl, getHttpUriRequest(httpUrl, header, params, timeout));
    }
}
