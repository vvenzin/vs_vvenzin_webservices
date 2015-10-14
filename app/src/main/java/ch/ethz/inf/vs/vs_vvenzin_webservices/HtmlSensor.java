package ch.ethz.inf.vs.vs_vvenzin_webservices;

import org.apache.http.client.methods.HttpGet;

public class HtmlSensor extends AbstractSensor {

    @Override
    protected void setHttpClient() {
        this.httpClient = SimpleHttpClientFactory.getInstance(SimpleHttpClientFactory.Type.LIB);
        this.setName("LibHttpClient");
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

        return  temperature;
    }

    @Override
    public void getTemperature() throws NullPointerException {
        // Build up a request to get a response with the temperature

        HttpGet request = new HttpGet(RemoteServerConfiguration.HOST /*+ "/sunspots/Spot1/sensors/temperature"*/);

        AsyncWorker worker = new AsyncWorker();
        worker.execute(request);
    }
}
