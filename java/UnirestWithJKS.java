package com.lendin.ib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class UnirestWithJKS {

  private static final String FILEPATH = "file.jks";
  private static final String PASSWORD = "password";

  public static void main(String[] args) throws UnirestException, UnrecoverableKeyException,
      KeyStoreException, NoSuchAlgorithmException, KeyManagementException, CertificateException,
      FileNotFoundException, IOException {

    SSLContext sslContext = setKeyStore();
    SSLConnectionSocketFactory csf =
        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    clientBuilder.setSSLSocketFactory(csf);
    HttpClient httpClient = clientBuilder.build();

    Unirest.setHttpClient(httpClient);
    String body = "";

    HttpResponse<String> response =
        Unirest.post("some url").header("Content-Type", "application/json").body(body).asString();
    System.out.println(response);
  }

  /** Set keystore with jks file and initialize sslcontext object */
  private static SSLContext setKeyStore()
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
      FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
    TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      @Override
      public void checkClientTrusted(X509Certificate[] certs, String authType) {}

      @Override
      public void checkServerTrusted(X509Certificate[] certs, String authType) {}
    }};

    SSLContext sslContext = SSLContext.getInstance("TLS");

    SecureRandom secureRandom = new SecureRandom();

    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(new FileInputStream(FILEPATH), PASSWORD.toCharArray());

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(ks, PASSWORD.toCharArray());
    sslContext.init(kmf.getKeyManagers(), trustAllCerts, secureRandom);

    SSLContext.setDefault(sslContext);
    return sslContext;
  }
}
