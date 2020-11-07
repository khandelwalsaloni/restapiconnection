package com.lendin.ib;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class URLConnectionWithJKS {

  private static final String FILEPATH = "file.jks";
  private static final String PASSWORD = "password";


  public static void main(String[] args) throws Exception {
    SSLContext sslContext = setKeyStore();
    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

    HostnameVerifier allHostsValid = new HostnameVerifier() {
      @Override
      public boolean verify(String hostname, SSLSession session) {
        return true;
      }
    };

    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    URL url = new URL("some url");
    URLConnection con = url.openConnection();

    con.setDoOutput(true);
    con.setRequestProperty("Content-Type", "application/json");
    String body = "";

    try (OutputStream output = con.getOutputStream()) {
      output.write(body.getBytes());
    }

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
