package com.lendin.ib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class HttpsURLConnectionJKSWithoutSSLContext {

  private static final String FILEPATH = "file.jks";
  private static final String PASSWORD = "password";

  public static void main(String[] args) throws IOException {

    System.setProperty("javax.net.ssl.keyStore", FILEPATH);
    System.setProperty("javax.net.ssl.keyStorePassword", PASSWORD);

    URL url = new URL("some url");
    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    con.setRequestMethod("POST");
    con.setDoOutput(true);

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
}
