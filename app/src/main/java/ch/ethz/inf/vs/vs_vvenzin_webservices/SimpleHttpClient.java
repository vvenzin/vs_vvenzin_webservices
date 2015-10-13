package ch.ethz.inf.vs.vs_vvenzin_webservices;

import java.lang.Object;import java.lang.String; /**
 * Classes that implement this interface should take an object that represents an HTTP request, execute this request and return its response.
 *
 * @author Leyna Sadamori
 *
 */
public interface SimpleHttpClient {
    /**
     * Execute the HTTP request and return the response
     * @param request HTTP request
     * @return HTTP response
     */
    public String execute(Object request);
}
