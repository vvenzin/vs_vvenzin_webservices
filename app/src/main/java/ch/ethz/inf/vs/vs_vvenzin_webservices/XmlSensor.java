package ch.ethz.inf.vs.vs_vvenzin_webservices;

import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.ksoap2.SoapFault;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.io.KXmlParser;

import android.util.Log;


/**
 * Created by Raphael on 14.10.2015.
 */
public class XmlSensor extends AbstractSensor  {

    String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";
    String METHOD_NAME = "getSpot(Spot3)";
    String SOAP_ACTION = METHOD_NAME;
    String URL = "http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice";
    SoapSerializationEnvelope envelope;

    public double parseResponse(String response) {


        double temperature = Double.parseDouble(response);
       /*
        try {
            XmlPullParser xp = new KXmlParser();
            xp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            xp.setInput(is, "UTF-8");
            envelope.parse(xp);
        } catch (Throwable e) {
            Log.e("###", "Error reading/parsing SOAP response", e);
            throw e;
        }
        double response=Double.parseDouble(envelope.bodyIn.toString());*/
        return temperature;
    }

    /**
     * Sends SOAP request to the web service.
     *
     * @param requestContent the SOAP request XML
     * @return KvmSerializable object generated from the SOAP response XML
     * @throws Exception if the web service can not be
     * reached, or the response data can not be processed.
     */
    public Object sendSoapRequest(String requestContent) {

        Object bodyIn = null;
        // send SOAP request
        try{
            String responseIs = sendRequest(requestContent);
            // create the response SOAP envelope
            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            envelope.encodingStyle = "UTF-8";
            bodyIn = envelope.getResponse();
            if (bodyIn instanceof SoapFault) {
                return null;
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }

        finally {
            return bodyIn;
        }
    }

    /**
     * Sends SOAP request to the web service.
     *
     * @param requestContent the content of the request
     * @return {@link InputStream} containing the response content
     * @throws Exception if communication with the web service
     * can not be established, or when the response from the service can not be
     * processed.
     */
    private String sendRequest(String requestContent) throws Exception {

        // initialize HTTP post
        HttpPost httpPost = new HttpPost(URL);


        // load content to be sent
            HttpEntity postEntity = new StringEntity(METHOD_NAME, "UTF-8");
            httpPost.setEntity(postEntity);


        httpPost.addHeader("Accept", "text/xml");
        httpPost.addHeader("SOAPAction", SOAP_ACTION);
        String httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
        } catch (Throwable e) {
            Log.e("###", "Error sending SOAP request", e);
            throw e;
        }


            /*
            // get the response content
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream is = httpEntity.getContent();
            return is;
        } catch (Throwable e) {
            Log.e("###", "Error getting SOAP response", e);
            throw e;
        }*/
        return httpResponse;
    }

    @Override
    protected void setHttpClient() {
        this.httpClient = SimpleHttpClientFactory.getInstance(SimpleHttpClientFactory.Type.SOAP);
        this.setName("httpClient");
    }

    @Override
    public void getTemperature() throws NullPointerException  {
        // Build up a request to get a response with the temperature
        String request="";
        Object response = sendSoapRequest(request);
        if(response==null){
            throw new NullPointerException();
        }
        parseResponse(response.toString());

        AsyncWorker worker = new AsyncWorker();
        worker.execute(request);
    }
}
