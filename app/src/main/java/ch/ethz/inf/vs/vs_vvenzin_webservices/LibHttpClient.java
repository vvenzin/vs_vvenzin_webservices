package ch.ethz.inf.vs.vs_vvenzin_webservices;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class LibHttpClient implements SimpleHttpClient {

    @Override
    public String execute(Object request) {
        HttpClient libHttpClient = new DefaultHttpClient();
        //HttpClient libHttpClient = HttpClientBuilder.create().build();
        String responseString = null;

        try {
            HttpResponse response = libHttpClient.execute(HttpHost.create("vslab.inf.ethz.ch"), (HttpRequest) request);

            // Get String from inputStream
            char[] b = new char[4096];
            final StringBuilder sb = new StringBuilder();
            final Reader in = new InputStreamReader(response.getEntity().getContent(), "UTF-8");
            for(;;) {
                int rsz = in.read(b, 0, b.length);
                if(rsz < 0) {
                    break;
                }
                sb.append(b, 0, rsz);
            }
            in.close();
            responseString = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseString;
    }
}
