package com.example.demo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
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
 * @Title: RequestUtil.java
 * @Project: im_web
 * @Package: cn.sunline.tiny.web.util
 * @author: 作者 : Adam
 * @date 创建时间：2018年3月30日 下午4:18:05
 * @version 1.0
 */
public class RequestUtil {
    private static final String DEFAULT_AUTHORIZATION = "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0";
    
    private static final String UTF8 = "UTF-8";
    private static final String PROTOCOL_HTTPS = "https";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    
    
    private static final  Logger LOG  =  LoggerFactory.getLogger(RequestUtil.class);
    
    private static int defaultTimeout = 3000;
    
    private static CloseableHttpClient getCloseableHttpClient() {
        return getCloseableHttpClient(false);
    }
    
    private static CloseableHttpClient getCloseableHttpClient(String httpUrl) throws MalformedURLException {
        return getCloseableHttpClient(isHttps(httpUrl));
    }
    private static CloseableHttpClient getCloseableHttpClient(boolean isHttps) {
        if(isHttps) {
            SSLContext sslcontext;
            try {
                sslcontext = SSLContexts.custom()
                        .loadTrustMaterial(null, (chain, authType) -> true)
                        .build();
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
//                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                return HttpClients.custom()
                        .setSSLSocketFactory(socketFactory)
                        .build();
            } catch (Exception e) {
//                e.printStackTrace();
                LOG.error("build HttpClient failed", e);
            }
        } else {
            return HttpClients.custom()
                    .build();
        }
        return null;
    }
    
    private static RequestConfig getRequestConfig() {
        return getRequestConfig(defaultTimeout);
    }
    
    private static RequestConfig getRequestConfig(int timeout) {
        return RequestConfig.custom()  
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)  
                .setSocketTimeout(timeout)
                .build(); 
    }
    
    private static boolean isHttps(String httpUrl) throws MalformedURLException {
        URL url = new URL(httpUrl);
        return "https".equals(url.getProtocol());
    }
    
    public static String getResponseContent(HttpResponse httpResponse) {
     // 响应内容
        String responseContent = null;
        if (httpResponse == null) {
            return responseContent;
        }
     // 获取响应实体
        HttpEntity entity = httpResponse.getEntity();

        if (null != entity) {
            // responseContent = EntityUtils.toString(entity);
            try {
                responseContent = EntityUtils.toString(entity, "UTF-8");
                // Consume response content
                EntityUtils.consume(entity);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }
    
    public static HttpResponse doRequest(String httpUrl, Map<String, String> header, String params, Integer timeout) {
            return doRequest(httpUrl, getHttpUriRequest(httpUrl, header, params, timeout));
    }
    public static HttpResponse doRequest(String httpUrl, Map<String, String> params, Integer timeout, Map<String, String> header) {
            return doRequest(httpUrl, getHttpUriRequest(httpUrl, params, timeout, header));
    }
    
    private static HttpResponse doRequest(String httpUrl, HttpUriRequest httpUriRequest) {
        try {
            return doRequest(getCloseableHttpClient(httpUrl), httpUriRequest);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static HttpResponse doRequest(CloseableHttpClient httpClient, HttpUriRequest httpUriRequest) {
        if (httpClient == null || httpUriRequest == null) {
            return null;
        }
        try {
            return httpClient.execute(httpUriRequest);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static HttpUriRequest getHttpUriRequest(String httpUrl, Map<String, String> header, String params, Integer timeout) {
        HttpRequestBase httpRequestBase = null;
        if (timeout == null || timeout < defaultTimeout) {
            timeout = defaultTimeout;
        }
        RequestConfig requestConfig = getRequestConfig(timeout);
        if (params != null) {
            HttpPost httpPost = new HttpPost(httpUrl);
            StringEntity stringEntity = new StringEntity(params, "UTF-8");
            httpPost.setEntity(stringEntity);
            httpRequestBase = httpPost;
        } else {
            httpRequestBase = new HttpGet(httpUrl);
        }
        if (httpRequestBase != null) {
            httpRequestBase.setConfig(requestConfig);
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpRequestBase.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        return httpRequestBase;
    }
    private static HttpUriRequest getHttpUriRequest(String httpUrl, Map<String, String> params, Integer timeout, Map<String, String> header) {
        HttpRequestBase httpRequestBase = null;
        if (timeout == null || timeout < defaultTimeout) {
            timeout = defaultTimeout;
        }
        RequestConfig requestConfig = getRequestConfig(timeout);
        if (params != null) {
            HttpPost httpPost = new HttpPost(httpUrl);
            // 构建POST请求的表单参数
            List<NameValuePair> formParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            httpRequestBase = httpPost;
        } else {
            httpRequestBase = new HttpGet(httpUrl);
        }
        if (httpRequestBase != null) {
            httpRequestBase.setConfig(requestConfig);
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpRequestBase.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        return httpRequestBase;
    }
    
    public static String doGet(String httpUrl, Integer timeout, Map<String, String> header) {
        return getResponseContent(doRequest(httpUrl, null, timeout, header));
    }
    
    public static String doPost(String httpUrl, Map<String, String> params, Integer timeout, Map<String, String> header) {
        return getResponseContent(doRequest(httpUrl, params, timeout, header));
    }

    public static String doJsonPost(String httpUrl, String param) {
        return doJsonPost(httpUrl, param, null);
    }
    
    public static String doJsonPost1(String httpUrl, String param) {
        return doJsonPost1(httpUrl, param, null);
    }

    public static String doJsonPost1(String httpUrl, String param, String authorization) {
        // 响应内容
        String responseContent = null;
        // 创建默认的httpClient实例
        CloseableHttpClient httpClient = null;
        try {
            httpClient = getCloseableHttpClient(httpUrl);
            // 创建HttpPost
            HttpPost httpPost = new HttpPost(httpUrl);
            httpPost.setConfig(getRequestConfig());
            
            StringEntity stringEntity = new StringEntity(param, "UTF-8");
            httpPost.setEntity(stringEntity);
            // 执行POST请求
            HttpResponse response = httpClient.execute(httpPost);

            // 获取响应实体
            HttpEntity entity = response.getEntity();

            if (null != entity) {
                // responseContent = EntityUtils.toString(entity);
                responseContent = EntityUtils.toString(entity, "UTF-8");
                // Consume response content
                EntityUtils.consume(entity);
            }
            
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                    LOG.debug("httpClient close failed");
                }
            }
        }
        return responseContent;
    }
    

    public static String doJsonPost(String httpUrl, String param, String authorization) {

        HttpURLConnection connection = null;
        HttpsURLConnection httpsURLConnection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(httpUrl);
            if (PROTOCOL_HTTPS.equals(url.getProtocol())) {
                SSLContext sc = createSslContext();
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setSSLSocketFactory(sc.getSocketFactory());
                httpsURLConnection.setHostnameVerifier((s, sslSession) -> true);
                httpsURLConnection.setRequestMethod(METHOD_POST);
                httpsURLConnection.setConnectTimeout(3000);
                httpsURLConnection.setReadTimeout(15000);
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
                if (authorization == null || authorization.isEmpty()) {
                    httpsURLConnection.setRequestProperty(HEADER_AUTHORIZATION, DEFAULT_AUTHORIZATION);
                } else {
                    httpsURLConnection.setRequestProperty(HEADER_AUTHORIZATION, authorization);
                }
                os = httpsURLConnection.getOutputStream();
                os.write(param.getBytes());
                os.flush();
                if (httpsURLConnection.getResponseCode() == HttpStatus.SC_OK) {
                    is = httpsURLConnection.getInputStream();
                }
            } else {
                // 通过远程url连接对象打开连接
                connection = (HttpURLConnection) url.openConnection();
                // 设置连接请求方式
                connection.setRequestMethod(METHOD_POST);
                // 设置连接主机服务器超时时间：3000毫秒
                connection.setConnectTimeout(3000);
                // 设置读取主机服务器返回数据超时时间：15000毫秒
                connection.setReadTimeout(15000);
                // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
                connection.setDoOutput(true);
                // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
                connection.setDoInput(true);
                // 设置传入参数的格式:请求参数应该是 json 字符串。
                connection.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
                // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
                if (authorization == null || authorization.isEmpty()) {
                    connection.setRequestProperty(HEADER_AUTHORIZATION, DEFAULT_AUTHORIZATION);
                } else {
                    connection.setRequestProperty(HEADER_AUTHORIZATION, authorization);
                }
                
                // 通过连接对象获取一个输出流
                os = connection.getOutputStream();
                // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
                os.write(param.getBytes());
                
                os.flush();
                // 通过连接对象获取一个输入流，向远程读取
                if (connection.getResponseCode() == HttpStatus.SC_OK) {
                    
                    is = connection.getInputStream();
                }
            }
            // 对输入流对象进行包装:charset根据工作项目组的要求来设置
//            br = new BufferedReader(new InputStreamReader(is));
            br = new BufferedReader(new InputStreamReader(is, UTF8));

            StringBuffer sbf = new StringBuffer();
            String temp = null;
            // 循环遍历一行一行读取数据
            while ((temp = br.readLine()) != null) {
                System.out.println(temp);
                sbf.append(temp);
                sbf.append("\r\n");
            }
            result = sbf.toString();
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
            // 断开与远程地址url的连接
            if (connection != null) {
                connection.disconnect();
            }
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }
        return result;
    }

    private static SSLContext createSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");
//        SSLContext sc = SSLContext.getInstance("SSL");

        sc.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());

        return sc;
    }
    
    public static String doGet(String httpUrl) {
        return doGet(httpUrl, null);
    }

    public static String doGet(String httpUrl, String authorization) {
        HttpURLConnection connection = null;
        HttpsURLConnection httpsURLConnection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpUrl);
            if (PROTOCOL_HTTPS.equals(url.getProtocol())) {
                SSLContext sc = createSslContext();
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setSSLSocketFactory(sc.getSocketFactory());
                httpsURLConnection.setHostnameVerifier((s, sslSession) -> true);
                httpsURLConnection.setRequestMethod(METHOD_GET);
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setDoInput(true);
                if (authorization == null || authorization.isEmpty()) {
                    httpsURLConnection.setRequestProperty(HEADER_AUTHORIZATION, DEFAULT_AUTHORIZATION);
                } else {
                    httpsURLConnection.setRequestProperty(HEADER_AUTHORIZATION, authorization);
                }
                httpsURLConnection.connect();
                if (httpsURLConnection.getResponseCode() == HttpStatus.SC_OK) {
                    is = httpsURLConnection.getInputStream();
                }
            } else {
                
                // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
                connection = (HttpURLConnection) url.openConnection();
                // 设置连接方式：get
                connection.setRequestMethod(METHOD_GET);
                // 设置连接主机服务器的超时时间：15000毫秒
                connection.setConnectTimeout(3000);
                // 设置读取远程返回的数据时间：60000毫秒
                connection.setReadTimeout(10000);
                // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
                if (authorization == null || authorization.isEmpty()) {
                    connection.setRequestProperty(HEADER_AUTHORIZATION, DEFAULT_AUTHORIZATION);
                } else {
                    connection.setRequestProperty(HEADER_AUTHORIZATION, authorization);
                }
                // 发送请求
                connection.connect();
                // 通过connection连接，获取输入流
                if (connection.getResponseCode() == HttpStatus.SC_OK) {
                    is = connection.getInputStream();
                }
            }
            // 封装输入流is，并指定字符集
            br = new BufferedReader(new InputStreamReader(is, UTF8));
            // 存放数据
            StringBuffer sbf = new StringBuffer();
            String temp = null;
            while ((temp = br.readLine()) != null) {
                sbf.append(temp);
                sbf.append("\r\n");
            }
            result = sbf.toString();
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
            // 关闭远程连接
            if (connection != null) {
                connection.disconnect();
            }
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }

        return result;
    }

    public static String getRequestParameter(HttpServletRequest request, String param) {
        if (param == null || param.isEmpty()) {
            return "";
        }
        String value = request.getParameter(param);
        if (value == null) {
            return "";
        }
        value = value.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (request.getMethod().equals(METHOD_GET)) {
            try {
                value = new String(value.getBytes("iso8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        return value;
    }
}