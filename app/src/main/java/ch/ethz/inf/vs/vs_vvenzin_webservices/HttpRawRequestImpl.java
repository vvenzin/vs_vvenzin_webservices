package ch.ethz.inf.vs.vs_vvenzin_webservices;

import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;

public class HttpRawRequestImpl implements HttpRawRequest {

    private String hostAddress, absPath;
    private int destPort;

    /**
     * @param hostAddress String representation of the host address
     * @param destPort Destination port
     * @param absPath Absolute path of the requested resource
     */
    HttpRawRequestImpl(String hostAddress, int destPort, String absPath) {
        this.hostAddress = hostAddress;
        this.destPort = destPort;
        this.absPath = absPath;
    }

    @Override
    public String generateRequest() {
        // StringBuilder to build up the request
        StringBuilder sb = new StringBuilder();

        // Carriage return and newline
        String endOfLine = "\r\n";

        // Initial line: GET
        sb.append("GET " + absPath + " HTTP/1.1" + endOfLine);
        // Header line host to specify the domain name of the server
        sb.append("HOST: " + hostAddress + ": " + endOfLine);
        // Empty line to signal end of header
        sb.append(endOfLine);

        return sb.toString();
    }

    @Override
    public String getHost() {
        return hostAddress;
    }

    @Override
    public int getPort() {
        return destPort;
    }
}
