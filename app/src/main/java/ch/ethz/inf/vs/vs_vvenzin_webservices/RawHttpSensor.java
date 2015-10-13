package ch.ethz.inf.vs.vs_vvenzin_webservices;

/**
 * Created by oliver on 13.10.15.
 */
public class RawHttpSensor extends AbstractSensor {
    @Override
    protected void setHttpClient() {

    }

    @Override
    public double parseResponse(String response) {
        return 0;
    }

    @Override
    public void getTemperature() throws NullPointerException {

    }
}
