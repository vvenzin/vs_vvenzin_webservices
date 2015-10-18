package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.HttpClient;

/**
 * Created by Raphael on 17.10.2015.
 */
public class SOAPHttpClient implements SimpleHttpClient {
    String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";
    String SOAP_ACTION =  NAMESPACE + "getSpot";
    String URL = "http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice";
    String SoapRequestXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <S:Header/>\n" +
            "    <S:Body>\n" +
            "        <ns2:getSpot xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\">\n" +
            "            <id>spot3</id>\n" +
            "        </ns2:getSpot>\n" +
            "    </S:Body>\n" +
            "</S:Envelope>";

    public String execute(Object request){
        HttpClient soapHttpClient = new DefaultHttpClient();
        // initialize HTTP post
        HttpPost httpPost = new HttpPost(URL);
        String response = null;

        // load content to be sent
        StringEntity se = new StringEntity(SoapRequestXML, HTTP.UTF_8);
        se.setContentType("text/xml");

        httpPost.addHeader("Accept", "text/xml");
        httpPost.addHeader("SOAPAction",SOAP_ACTION);

        httpPost.setEntity(se);

        HttpResponse httpResponse = null;
        try {
            httpResponse = soapHttpClient.execute(httpPost);
        } catch (Exception e) {
            Log.e("###", "Error sending SOAP request", e);
            e.printStackTrace();
        }



        // get the response content
        HttpEntity httpEntity = httpResponse.getEntity();


        try {
            response = EntityUtils.toString(httpEntity);
        }catch (Exception e){
            e.printStackTrace();
        }

        return response;
    }
}
