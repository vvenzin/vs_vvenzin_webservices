package ch.ethz.inf.vs.vs_vvenzin_webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.net.Socket;

public class RawHttpClient implements SimpleHttpClient {

    private String hostAddress;
    private int destPort;

    /**
     * Execute the HTTP request and return the response
     * @param request HTTP request
     * @return HTTP response
     */
    @Override
    public String execute(Object request) {

        Socket socket;
        OutputStream outputStream;
        InputStream inputStream;

        String response;

        socket = null;
        response = "";

        // Get the host address and the destination port from the request???

        try {
            // Initialize the socket and the streams
            socket = new Socket(hostAddress, destPort);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            // Wrap the socket output stream and write the request
            PrintWriter out = new PrintWriter(outputStream);
            out.print(request);
            out.flush();

            byte[] b = new byte[1024];
            inputStream.read(b);

            response = new String(b);

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}