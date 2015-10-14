package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.util.Log;

public class RawHttpSensor extends AbstractSensor {

    @Override
    protected void setHttpClient() {
        this.httpClient = SimpleHttpClientFactory.getInstance(SimpleHttpClientFactory.Type.RAW);
        this.setName("RawHttpClient");
    }

    @Override
    public double parseResponse(String response) {
        double temperature;

        char[] resp;
        char[] temp = new char[5];
        resp = response.toCharArray();

        int i;
        for(i = 0; i < 5; i++) {
            if(!(resp[985 + i] == '<')) {
                temp[i] = resp[985 + i];
            }
        }

        try {
            temperature = Double.parseDouble(new String(temp));
        } catch (NumberFormatException nfe) {
             temperature = Double.NaN;
        }
        Log.d("#### VV ####", "RESTClientActivity - ReturnRaw()");
        return  temperature;
    }

    @Override
    public void getTemperature() throws NullPointerException {
        // Build up a request to get a response with the temperature
        String request;
        request = HttpRawRequestFactory.getInstance
                (RemoteServerConfiguration.HOST, RemoteServerConfiguration.REST_PORT,
                        "/sunspots/Spot1/sensors/temperature")
                .generateRequest();

        AsyncWorker worker = new AsyncWorker();
        worker.execute(request);
    }
}
