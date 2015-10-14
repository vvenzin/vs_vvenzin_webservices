package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.util.Log;

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

        // Char array carrying the response
        char[] resp;
        resp = response.toCharArray();

        // Char array carrying the temperature
        char[] temp = new char[5];

        // Hard coded: Copy temperature from resp to temp
        int i;
        for(i = 0; i < 5; i++) {
            if(!(resp[853 + i] == '<')) {
                temp[i] = resp[853 + i];
            }
        }

        // Parsing the char array, if not successful set the temperature to Double.NaN
        try {
            temperature = Double.parseDouble(new String(temp));
        } catch (NumberFormatException nfe) {
            temperature = Double.NaN;
        }

        Log.d("#### VV ####", "RESTClientActivity - ReturnLib()");
        return  temperature;
    }

    @Override
    public void getTemperature() throws NullPointerException {
        HttpGet request = new HttpGet("http://" + RemoteServerConfiguration.HOST
                +":8081" + "/sunspots/Spot1/sensors/temperature");

        AsyncWorker worker = new AsyncWorker();
        worker.execute(request);
    }
}
