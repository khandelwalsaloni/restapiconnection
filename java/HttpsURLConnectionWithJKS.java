package com.lendin.ib;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

public class HttpsURLConnectionWithJKS {

  private static final String FILEPATH = "file.jks";
  private static final String PASSWORD = "password";

  public static void main(String[] args)
      throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
      NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {

    SSLContext sslContext = setKeyStore();

    HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

    URL url = new URL("some url");
    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    con.setRequestMethod("POST");
    con.setDoOutput(true);
    con.setSSLSocketFactory(sslContext.getSocketFactory());

    // set request headers
    con.setRequestProperty("Content-Type", "application/json");

    // set request body
    String body = "some body";
    OutputStream wr = con.getOutputStream();
    wr.write(body.getBytes());

    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line + "\n");
    }
    br.close();
    System.out.println(sb.toString());
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
