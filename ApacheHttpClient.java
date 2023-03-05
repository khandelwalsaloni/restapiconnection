import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ApacheHttpClient {
    public static void main(String[] args) throws IOException {
	String body = "";

        HttpClient apacheClient = HttpClients.custom().build();
        HttpPost request = new HttpPost("some url");

        request.setHeader("Content-Type", "application/json");
	request.setEntity(new StringEntity(body));

        HttpResponse response = apacheClient.execute(request);
        
        System.out.println(response.getEntity());
	// To print in string
	System.out.println(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
    }
}
