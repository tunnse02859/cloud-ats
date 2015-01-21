/**
 * 
 */
package org.ats.common.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * 
 */
public class HttpClientFactory {
  /** . */
  public static int MAX_HTTP_CONNECTION = 3000;

  public static CloseableHttpClient getInstance() {
      return createNewDefaultHttpClient();
  }

  public static CloseableHttpClient createNewDefaultHttpClient() {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    clientBuilder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.4) Gecko/20100513 Firefox/3.6.4");
    
    RequestConfig requestConfig = RequestConfig.custom()
        .setAuthenticationEnabled(true)
        .setCircularRedirectsAllowed(true)
        .setConnectionRequestTimeout(1 * 60 * 1000)
        .setConnectTimeout(1 * 60 * 1000)
        .setCookieSpec(CookieSpecs.BEST_MATCH)
        .setSocketTimeout(1 * 60 * 1000)
        .setStaleConnectionCheckEnabled(false).build();
    clientBuilder.setDefaultRequestConfig(requestConfig);
    
    SocketConfig socketConfig = SocketConfig.custom()
        .setSoKeepAlive(true)
        .setSoTimeout(1 * 60 * 1000)
        .setTcpNoDelay(true).build();
    clientBuilder.setDefaultSocketConfig(socketConfig);
    
    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(1, TimeUnit.MINUTES);
    connManager.setDefaultMaxPerRoute(4);
    connManager.setMaxTotal(MAX_HTTP_CONNECTION);
    
    ConnectionConfig connConfig = ConnectionConfig.custom()
        .setCharset(Charset.forName("UTF-8")).build();
    connManager.setDefaultConnectionConfig(connConfig);
    
    clientBuilder.setConnectionManager(connManager);
    
    HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
      public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (executionCount > 2) return false;
        if (exception instanceof NoHttpResponseException) return true;
        if (exception instanceof SSLHandshakeException) return false;
        HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
        if (!(request instanceof HttpEntityEnclosingRequest)) return true;
        return false;
      }
    };
    clientBuilder.setRetryHandler(retryHandler);
    
    HttpRequestInterceptor requestInterceptor = new HttpRequestInterceptor() {
      public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (!request.containsHeader("Accept-Encoding"))
          request.addHeader("Accept-Encoding", "gzip, deflate");
      }
    };
    clientBuilder.addInterceptorFirst(requestInterceptor);
    
    HttpResponseInterceptor responseInterceptor = new HttpResponseInterceptor() {
      public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpEntity entity = response.getEntity();
        Header header = entity.getContentEncoding();
        if (header != null) {
          HeaderElement[] codecs = header.getElements();
          for (HeaderElement codec : codecs) {
            String codecName = codec.getName();
            if ("gzip".equalsIgnoreCase(codecName)) {
              response.setEntity(new GzipDecompressingEntity(entity));
              return;
            } else if ("deflate".equalsIgnoreCase(codecName)) {
              response.setEntity(new DeflateDecompressingEntity(entity));
              return;
            }
          }
        }
      }
    };
    clientBuilder.addInterceptorFirst(responseInterceptor);
    
    return clientBuilder.build();
  }
}
