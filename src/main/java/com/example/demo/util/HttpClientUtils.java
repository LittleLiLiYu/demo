package com.example.demo.util;


import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.alibaba.fastjson.JSON;

import net.minidev.json.JSONArray;
/**
 * <p>Title: HttpClientUtils</p>
 * <p>Description: </p>
 * <p>Company: sunline</p>
 * @author Kuangyu
 * @date 2019年3月11日
 * @version 1.0
 */
@SuppressWarnings("all")
public class HttpClientUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    /**
     * 向HTTPS地址发送POST请求
     *
     * @param reqURL 请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    public static String sendSSLPostRequest(String reqURL, Map<String, String> params) {
        return sendSSLPostRequest(reqURL, params, 3000);
    }

    /**
     * 向HTTPS地址发送POST请求
     *
     * @param reqURL 请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    public static HttpResponse sendSSLPostRequest2(String reqURL, Map<String, String> params) {
        return sendSSLPostRequest2(reqURL, params, 3000);
    }

    /**
     * 向HTTPS地址发送POST请求
     *
     * @param reqURL 请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    
    public static HttpResponse sendSSLPostRequest2(String reqURL, Map<String, String> params, int timeout) {
        // 响应内容
        String responseContent = null;
        // 这是超时时间
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);

        // 创建默认的httpClient实例
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        // 创建TrustManager
        X509TrustManager xtm = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        HttpResponse response = null;
        try {
            // TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
            SSLContext ctx = SSLContext.getInstance("TLS");

            // 使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
            ctx.init(null, new TrustManager[] { xtm }, null);

            // 创建SSLSocketFactory
            SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);

            // 通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));

            // 创建HttpPost
            HttpPost httpPost = new HttpPost(reqURL);

            // 构建POST请求的表单参数
            List<NameValuePair> formParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));

            // 执行POST请求
            response = httpClient.execute(httpPost);

            // 获取响应实体
            HttpEntity entity = response.getEntity();

            if (null != entity) {
                responseContent = EntityUtils.toString(entity, "UTF-8");
                // Consume response content
                EntityUtils.consume(entity);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            // 关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }

        return response;
    }

    /**
     * 向HTTPS地址发送POST请求
     *
     * @param reqURL 请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    public static String sendSSLPostRequest(String reqURL, Map<String, String> params, int timeout) {
        // 响应内容
        String responseContent = null;
        // 这是超时时间
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);

        // 创建默认的httpClient实例
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        // 创建TrustManager
        X509TrustManager xtm = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        try {
            // TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
            SSLContext ctx = SSLContext.getInstance("TLS");

            // 使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
            ctx.init(null, new TrustManager[] { xtm }, null);

            // 创建SSLSocketFactory
            SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);

            // 通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));

            // 创建HttpPost
            HttpPost httpPost = new HttpPost(reqURL);

            // 构建POST请求的表单参数
            List<NameValuePair> formParams = new ArrayList<NameValuePair>();
            if (null != params) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));

            // 执行POST请求
            HttpResponse response = httpClient.execute(httpPost);

            // 获取响应实体
            HttpEntity entity = response.getEntity();

            if (null != entity) {
                responseContent = EntityUtils.toString(entity, "UTF-8");
                // Consume response content
                EntityUtils.consume(entity);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            // 关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }

        return responseContent;
    }

    /**
     * 向HTTPS地址发送POST请求
     *
     * @param reqURL 请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    public static String sendSSLPostRequest3(String reqURL, String params, int timeout) {
        // 响应内容
        String responseContent = null;
        // 这是超时时间
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);

        // 创建默认的httpClient实例
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        // 创建TrustManager
        X509TrustManager xtm = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        try {
            // TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
            SSLContext ctx = SSLContext.getInstance("TLS");

            // 使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
            ctx.init(null, new TrustManager[] { xtm }, null);

            // 创建SSLSocketFactory
            SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);

            // 通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));

            // 创建HttpPost
            HttpPost httpPost = new HttpPost(reqURL);

            StringEntity se = new StringEntity(params, "UTF-8");
            // se.setContentEncoding("UTF-8");

            // RequestEntity requestEntity = new StringRequestEntity(params,
            // "text/html", "UTF-8");

            httpPost.setEntity(se);

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

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            // 关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }

        return responseContent;
    }

    /**
     * 向HTTPS地址发送POST请求(不校验根证书域名)
     *
     * @param reqURL 请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    public static String sendSSLPostRequestWithoutVerify(String reqURL, Map<String, String> params) {
        return sendSSLPostRequestWithoutVerify(reqURL, params, 3000);
    }

    /**
     * 向HTTPS地址发送POST请求(不校验根证书域名)
     *
     * @param reqURL 请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    @SuppressWarnings("deprecation")
    public static String sendSSLPostRequestWithoutVerify(String reqURL, Map<String, String> params, int timeout) {
        // 响应内容
        String responseContent = null;
        // 这是超时时间
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);

        // 创建默认的httpClient实例
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        // 创建TrustManager
        X509TrustManager xtm = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        try {
            // TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
            SSLContext ctx = SSLContext.getInstance("TLS");

            // 使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
            ctx.init(null, new TrustManager[] { xtm }, null);

            // 创建SSLSocketFactory
            SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);
            socketFactory.setHostnameVerifier(new X509HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                    return;
                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                    return;
                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                    return;
                }
            });

            // 通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));

            // 创建HttpPost
            HttpPost httpPost = new HttpPost(reqURL);

            // 构建POST请求的表单参数
            List<NameValuePair> formParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));

            // 执行POST请求
            HttpResponse response = httpClient.execute(httpPost);

            // 获取响应实体
            HttpEntity entity = response.getEntity();

            if (null != entity) {
                responseContent = EntityUtils.toString(entity, "UTF-8");
                // Consume response content
                EntityUtils.consume(entity);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            // 关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }

        return responseContent;
    }

    /**
     * 向HTTPS地址发送POST请求 传输附件
     *
     * @param serverUrl
     * @param fileParamName
     * @param serverUrl
     * @param params
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String postFile(String serverUrl, String fileParamName, InputStream stream,
                                  Map<String, String> params) throws ClientProtocolException, IOException {
        HttpPost httpPost = new HttpPost(serverUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // 上传的文件
        builder.addBinaryBody(fileParamName, stream);
        // 设置其他参数
        if (!params.isEmpty()) {
            for (Entry<String, String> entry : params.entrySet()) {
                builder.addTextBody(entry.getKey(), entry.getValue(), ContentType.TEXT_PLAIN.withCharset("UTF-8"));
            }
        }
        HttpEntity httpEntity = builder.build();
        httpPost.setEntity(httpEntity);
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(httpPost);
        if (null == response || response.getStatusLine() == null) {
            return null;
        } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            return null;
        }
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    /**
     * @Title:
     * @Description: Http处理文件流
     * @author LiHaiQing
     * @param:
     */
    public static byte[] sendHttpPostReturnBytes(String httpUrl, String params) {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).setConnectionRequestTimeout(60000).build();
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        byte[] responseContent = null;
        try {
            // 设置参数
            StringEntity stringEntity = new StringEntity(params, "UTF-8");
            stringEntity.setContentType(MediaType.TEXT_PLAIN_VALUE);
            httpPost.setEntity(stringEntity);
            // 创建默认的httpClient实例.
            httpClient = HttpClients.createDefault();
            httpPost.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            if (code != org.apache.http.HttpStatus.SC_OK) {
                logger.info("HuaYu get file response error ,httpStatusCode:" + code);
                throw new Exception("HuaYuFileHttpClientUtil.sendHttpPostReturnBytes post请求失败，,httpStatusCode:{"
                                               + code + "}");
            } else {
                entity = response.getEntity();
                responseContent = EntityUtils.toByteArray(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("HttpClientUtil.sendHttpPostReturnBytes post请求失败",e);
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("HttpClientUtil.sendHttpPostReturnBytes post请求失败",e);
            }
        }
        return responseContent;

    }

    public static void main(String[] args) throws ClientProtocolException, IOException {

        try {
            Map<String, String> params = new HashMap<>();
            params.put("jid", "admin@10.25.0.188");
            params.put("type", "xml");
//            params.put("username", "test130");
//            params.put("name", "test130");
//            params.put("password", "123456");
//            String result = HttpClientUtils.sendSSLPostRequest("https://10.25.0.188:9091/plugins/presence/status", params);
            
//            String result = HttpClientUtil.doPost("https://10.25.0.188:9091/plugins/presence/status", params);
            String result = HttpClientUtil.doGet("https://10.25.0.188:9091/plugins/presence/status?jid=admin@10.25.0.188&type=xml");
//            String result = RequestUtil.doPost("https://10.25.0.188:9091/plugins/presence/status", params, null, null);
//            String result = RequestUtil.doGet("https://10.25.0.188:9091/plugins/presence/status?jid=admin@10.25.0.188&type=xml", null, null);
//            String result = RequestUtil.doJsonPost("https://10.25.0.188:9091/plugins/tinyimserver/users/create", JSON.toJSONString(params));
//            String result = RequestUtil.doJsonPost1("https://10.25.0.188:9091/plugins/presence/status", JSON.toJSONString(params));
//            String result = RequestUtil.doJsonPost("https://10.25.0.188:9091/plugins/presence/status", JSON.toJSONString(params));
//            String result = HttpClientUtil.sendSSLGetRequest("https://10.25.0.188:9091/plugins/presence/status?jid=admin@10.25.0.188&type=xml");
            System.out.println(result);
        } catch (Exception e) {
        } finally {

        }
    }
}