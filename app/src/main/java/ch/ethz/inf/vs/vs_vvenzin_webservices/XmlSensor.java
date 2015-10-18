package ch.ethz.inf.vs.vs_vvenzin_webservices;


import android.util.Log;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

public class XmlSensor extends AbstractSensor  {

    String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";

    public double parseResponse(String in) {

    String stemperature = null;
        try {
            XmlPullParser xp = new KXmlParser();
            xp.setInput(new StringReader(in));
            int eventType = xp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                if(eventType == XmlPullParser.START_TAG){
                    if(xp.getName().equals("temperature")){
                        eventType = xp.next();
                        stemperature  = xp.getText();
                        break;
                    }
                    else eventType = xp.next();
                }
                else eventType = xp.next();
            }


        } catch (Exception e) {
            Log.e("###", "Error reading/parsing SOAP response", e);
            e.getStackTrace();
        }
        double dtemperature = Double.parseDouble(stemperature);
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
