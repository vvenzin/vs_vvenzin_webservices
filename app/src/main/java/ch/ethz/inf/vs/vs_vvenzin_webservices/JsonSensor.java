package ch.ethz.inf.vs.vs_vvenzin_webservices;


import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonSensor extends AbstractSensor {

    @Override
    protected void setHttpClient() {
        this.httpClient = SimpleHttpClientFactory.getInstance(SimpleHttpClientFactory.Type.LIB);
        this.setName("LibHttpClient");
    }

    @Override
    public double parseResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getDouble("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Double.NaN;
    }

    @Override
    public void getTemperature() throws NullPointerException {
        HttpGet request = new HttpGet("http://" + RemoteServerConfiguration.HOST
                + ":8081" + "/sunspots/Spot1/sensors/temperature");
        request.setHeader("Accept", "application/json");

        AsyncWorker worker = new AsyncWorker();
        worker.execute(request);
    }
}
