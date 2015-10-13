package ch.ethz.inf.vs.vs_vvenzin_webservices;

public class RawHttpSensor extends AbstractSensor {

    @Override
    protected void setHttpClient() {
        this.httpClient = SimpleHttpClientFactory.getInstance(SimpleHttpClientFactory.Type.RAW);
        this.setName("httpClient");
    }

    @Override
    public double parseResponse(String response) {
        double temperature;

        // Somehow get the temperature from the response
        temperature = 0;

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
