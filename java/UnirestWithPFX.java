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
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class UnirestWithPFX {

  private static final String FILEPATH = "file.jks";
  private static final String PASSWORD = "password";
  private static final String CACERTS_STORE_PATH = "jre/lib/security/cacerts";
  private static final String CACERTS_PASSWORD = "password";

  public static void main(String[] args) throws UnirestException, UnrecoverableKeyException,
      KeyStoreException, NoSuchAlgorithmException, KeyManagementException, CertificateException,
      FileNotFoundException, IOException {

    SSLContext sslContext = setKeyStore();
    SSLConnectionSocketFactory csf =
        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

    // HttpClient is required to send with Unirest
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    clientBuilder.setSSLSocketFactory(csf);
    HttpClient httpClient = clientBuilder.build();

    Unirest.setHttpClient(httpClient);
    String body = "";

    HttpResponse<String> response =
        Unirest.post("some url").header("Content-Type", "application/json").body(body).asString();
    System.out.println(response);
  }

  /** Set keystore with pfx file and initialize sslcontext object */
  private static SSLContext setKeyStore()
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
      FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(new FileInputStream(FILEPATH), PASSWORD.toCharArray());

    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, PASSWORD.toCharArray());

    KeyManager[] kms = kmf.getKeyManagers();

    // Insert the cert file to your cacerts store before doing this
    KeyStore trustStore = KeyStore.getInstance("JKS");
    trustStore.load(new FileInputStream(CACERTS_STORE_PATH), CACERTS_PASSWORD.toCharArray());

    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(trustStore);
    TrustManager[] tms = tmf.getTrustManagers();

    SecureRandom secureRandom = new SecureRandom();

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kms, tms, secureRandom);
    SSLContext.setDefault(sslContext);

    return sslContext;
  }
}
