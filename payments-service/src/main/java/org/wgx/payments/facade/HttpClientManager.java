package org.wgx.payments.facade;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * Http client manager.
 *
 */
@SuppressWarnings("deprecation")
public final class HttpClientManager {

    private static DefaultHttpClient client;
    private static DefaultHttpClient longtimeClient;

    private HttpClientManager() {
        SchemeRegistry sr = SchemeRegistryFactory.createDefault();
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(sr);
        cm.setMaxTotal(1000);
        cm.setDefaultMaxPerRoute(100);
        client = new DefaultHttpClient(cm);
        client.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
        client.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        longtimeClient = new DefaultHttpClient();
        longtimeClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
        longtimeClient.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
        longtimeClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000 * 100);
        longtimeClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000 * 100);
    }

    /**
     * Send HTTP POST request to server.
     * @param url Server url address.
     * @param charset Charset.
     * @param bytes Content needs to be sent.
     * @return Result.
     * @throws ClientProtocolException ClientProtocolException
     * @throws IOException IOException
     */
    public static String httpPostBytes(final String url, final String charset, final byte[] bytes) throws ClientProtocolException, IOException {
        HttpPost filePost = new HttpPost(url);
        ByteArrayEntity entity = new ByteArrayEntity(bytes);
        filePost.setEntity(entity);
        HttpResponse response = getHttpClient().execute(filePost);
        String text = EntityUtils.toString(response.getEntity(), charset);
        try {
            response.getEntity().getContent().close();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
     * Send HTTP GET request to server.
     * @param url Server url address.
     * @return Result.
     * @throws ClientProtocolException ClientProtocolException
     * @throws IOException IOException
     */
    public static String httpGet(final String url) throws ClientProtocolException, IOException {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = getHttpClient().execute(httpGet);
        String text = EntityUtils.toString(response.getEntity());
        try {
            response.getEntity().getContent().close();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
     * Send HTTP POST request to server.
     * @param url Server url address.
     * @param parameters HTTP POST parameters;
     * @return Result.
     * @throws ClientProtocolException ClientProtocolException
     * @throws IOException IOException
     */
    public static String httpPost(final String url, final List<NameValuePair> parameters) throws ClientProtocolException, IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        HttpResponse response = getHttpClient().execute(httpPost);
        String text = EntityUtils.toString(response.getEntity());
        try {
            response.getEntity().getContent().close();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
     * Post HTTP POST request with XML content.
     * @param xmlString XML content.
     * @param url URL to post request.
     * @return Response.
     * @throws ClientProtocolException ClientProtocolException
     * @throws IOException IOException
     */
    public static String httpPostXML(final String xmlString, final String url) throws ClientProtocolException, IOException {
        HttpPost httpPost = new HttpPost(url);
        StringEntity postEntity = new StringEntity(xmlString, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.setEntity(postEntity);
        HttpResponse response = wrapClient(getHttpClient()).execute(httpPost);
        String text = EntityUtils.toString(response.getEntity(), "UTF-8");
        try {
            response.getEntity().getContent().close();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return text;
    }

    //CHECKSTYLE:OFF
    private static HttpClient wrapClient(final HttpClient base) {
        try (InputStream instream = HttpClientManager.class.getClassLoader().getResource("apiclient_cert.p12").openStream()) {
            KeyStore trustStore  = KeyStore.getInstance("PKCS12");
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(trustStore, "1285445301".toCharArray());
            Set<KeyManager> keymanagers = new HashSet<KeyManager>();
            final KeyManager[] kms =  kmfactory.getKeyManagers();
            if (kms != null) {
                for (final KeyManager km : kms) {
                    keymanagers.add(km);
                }
            }

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(!keymanagers.isEmpty() ? keymanagers.toArray(new KeyManager[keymanagers.size()]) : null,
                    null, null);
            SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext);
            Scheme sch = new Scheme("https", 443, socketFactory);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(sch);
            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception e) {
            return base;
        }
    }
    //CHECKSTYLE:ON

    /**
     * Entrance.
     * @return HttpClientManager instance.
     */
    public static HttpClient getHttpClient() {
        if (client == null) {
            synchronized (HttpClientManager.class) {
                new HttpClientManager();
            }
        }
        return client;
    }
}
