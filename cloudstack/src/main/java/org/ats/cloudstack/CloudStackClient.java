/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 24, 2014
 */
class CloudStackClient {
  
  /** .*/
  private final String host;

  /** .*/
  private final String apiKey;
  
  /** .*/
  private final String secretKey;
  
  private CloudStackClient() throws IOException {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cs.properties");
    Properties csProps = new Properties();
    csProps.load(is);
    this.host = csProps.getProperty("host");
    this.apiKey = csProps.getProperty("api-key");
    this.secretKey = csProps.getProperty("secret-key");
  }
  
  public CloudStackClient(String host, String apiKey, String secretKey) {
    this.host = host;
    this.apiKey = apiKey;
    this.secretKey = secretKey;
  }
  
  public String getHost() {
    return host;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  /**
   * 
   * @param command ex: "command=listUsers"
   * @return final url with signature
   * @throws UnsupportedEncodingException 
   */
   String buildCommandURL(String command) throws UnsupportedEncodingException {
    // Step 1: Make sure your APIKey is toLowerCased and URL encoded
       String encodedApiKey = URLEncoder.encode(apiKey.toLowerCase(), "UTF-8");
       
       // Step 2: toLowerCase all the parameters, URL encode each parameter value, and the sort the parameters in alphabetical order
       // Please note that if any parameters with a '&' as a value will cause this test client to fail since we are using '&' to delimit 
       // the string
       List<String> sortedParams = new ArrayList<String>();
       sortedParams.add("apikey="+encodedApiKey);
       StringTokenizer st = new StringTokenizer(command, "&");
       while (st.hasMoreTokens()) {
         String paramValue = st.nextToken().toLowerCase();
         String param = paramValue.substring(0, paramValue.indexOf("="));
         String value = URLEncoder.encode(paramValue.substring(paramValue.indexOf("=")+1, paramValue.length()), "UTF-8").replaceAll("\\+", "%20");
         sortedParams.add(param + "=" + value);
       }
       Collections.sort(sortedParams);
       
       // Step 3: Construct the sorted URL and sign and URL encode the sorted URL with your secret key
       String sortedUrl = null;
       boolean first = true;
       for (String param : sortedParams) {
         if (first) {
           sortedUrl = param;
           first = false;
         } else {
           sortedUrl = sortedUrl + "&" + param;
         }
       }
       String encodedSignature = signRequest(sortedUrl, secretKey);
       
       // Step 4: Construct the final URL we want to send to the CloudStack Management Server
       // Final result should look like:
       // http(s)://://client/api?&apiKey=&signature=

       String finalUrl = host + "?" + command + "&apiKey=" + apiKey  + "&signature=" + encodedSignature;
       return finalUrl;
  }
  
  /**
   * 1. Signs a string with a secret key using SHA-1
   * 2. Base64 encode the result
   * 3. URL encode the final result
   * 
   * @param request
   * @param key
   * @return
   */
  private String signRequest(String request, String key) {
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
      mac.init(keySpec);
      mac.update(request.getBytes());
      byte[] encryptedBytes = mac.doFinal();
      return URLEncoder.encode(Base64.encodeBase64String(encryptedBytes), "UTF-8");
    } catch (Exception ex) {
      System.out.println(ex);
    }
    return null;
  }

  public static CloudStackClient getInstance() throws IOException {
    return new CloudStackClient();
  }
}
