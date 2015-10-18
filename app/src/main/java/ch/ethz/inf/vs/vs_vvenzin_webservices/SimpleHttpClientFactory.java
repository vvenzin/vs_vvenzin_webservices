package ch.ethz.inf.vs.vs_vvenzin_webservices;

public abstract class SimpleHttpClientFactory {
    public static SimpleHttpClient getInstance(Type type) {
        switch (type) {
            case RAW:
                return new RawHttpClient();
            case LIB:
                return new LibHttpClient();
            case SOAP:
                return new SOAPHttpClient();
            default:
                return null;
        }
    }

    public enum Type {
        RAW, LIB, SOAP;
    }
}
