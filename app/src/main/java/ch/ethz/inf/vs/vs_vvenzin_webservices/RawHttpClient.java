package ch.ethz.inf.vs.vs_vvenzin_webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;

public class RawHttpClient implements SimpleHttpClient {

    /**
     * Execute the HTTP request and return the response
     * @param request HTTP request
     * @return HTTP response
     */
    @Override
    public String execute(Object request) {

        // Declare locals;
        String hostAddress;
        int destPort;
        Socket socket;
        OutputStream outputStream;
        InputStream inputStream;
        String response;
        
        // Initialize locals
        hostAddress = RemoteServerConfiguration.HOST;
        destPort = RemoteServerConfiguration.REST_PORT;
        response = null;

        try {
            // Initialize the socket and the streams
            socket = new Socket(hostAddress, destPort);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            // Wrap the  outputStream and write the request
            PrintWriter out = new PrintWriter(outputStream);
            out.print(request);
            out.flush();

            // Get String from inputStream
            char[] b = new char[4096];
            final StringBuilder sb = new StringBuilder();
            final Reader in = new InputStreamReader(inputStream, "UTF-8");
            for(;;) {
                int rsz = in.read(b, 0, b.length);
                if(rsz < 0) {
                    break;
                }
                sb.append(b, 0, rsz);
            }
            in.close();
            response = sb.toString();

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}