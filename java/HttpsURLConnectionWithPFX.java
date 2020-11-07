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
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

public class HttpsURLConnectionWithPFX {

  private static final String FILEPATH = "file.pfx";
  private static final String PASSWORD = "password";
  private static final String CACERTS_STORE_PATH = "jre/lib/security/cacerts";
  private static final String CACERTS_PASSWORD = "password";

  public static void main(String[] args) throws ClientProtocolException, IOException,
      CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
      UnrecoverableKeyException, NoSuchProviderException {

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
