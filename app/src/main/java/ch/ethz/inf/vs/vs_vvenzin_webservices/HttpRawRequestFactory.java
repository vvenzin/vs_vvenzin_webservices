package ch.ethz.inf.vs.vs_vvenzin_webservices;

public class HttpRawRequestFactory {
	public static HttpRawRequest getInstance(String host, int port, String path) {
		// return HttpRawRequest implementation
		return new HttpRawRequestImpl(host, port, path);
	}
}
