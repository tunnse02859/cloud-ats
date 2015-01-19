/**
 * 
 */
package org.ats.common.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

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
    return clientBuilder.build();
  }
}
