package ch.ethz.inf.vs.vs_vvenzin_webservices;


import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;

import org.kxml2.io.KXmlParser;

import android.util.Log;


/**
 * Created by Raphael on 14.10.2015.
 */
public class XmlSensor extends AbstractSensor  {

    String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";

    public double parseResponse(String response) {

    String stemerature = null;
        try {
            XmlPullParser xp = new KXmlParser();
            xp.setInput(new StringReader(response));
            stemerature= xp.getAttributeValue(NAMESPACE, "temperature");


        } catch (Exception e) {
            Log.e("###", "Error reading/parsing SOAP response", e);
            e.getStackTrace();
        }
        double dtemperature = Double.parseDouble(stemerature);
        return dtemperature;
    }

    @Override
    protected void setHttpClient() {
        this.httpClient = SimpleHttpClientFactory.getInstance(SimpleHttpClientFactory.Type.SOAP);
        this.setName("httpClient");
    }

    @Override
    public void getTemperature() throws NullPointerException  {
        // Build up a request to get a response with the temperature

        AsyncWorker worker = new AsyncWorker();
        worker.execute(httpClient);
    }
}
