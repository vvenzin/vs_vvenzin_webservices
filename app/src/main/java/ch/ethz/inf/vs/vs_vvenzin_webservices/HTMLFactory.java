package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;


/**
 * Created by Valentin on 16/10/15.
 *
 * This class provides all HTML files for the server which is registered as listener.
 * Usually it does so by calling a callback method of the listener. The reason for this is that
 * Sensors may take a while to respond.
 *
 * However there is the possibility to instantly get a html file if needed.
 *
 * This class carries out the effect of certain requests, i.e. from actuators.
 *
 */

public class HTMLFactory implements SensorEventListener {


    private final String FLASHLIGHT = "flashlight";
    private final String HTTP_RESPONSE = "HTTP/1.1 200 OK\r\n\r\n";
    private final String DEFAULT_SENSOR_NAME = "Please select a sensor above";

    // Documents
    private final String PARENT = "index";
    private final String SENSOR = "sensor";
    private final String ACTUATOR = "actuators";
    private final String VIBRATOR = "vibrator";
    private final String SORRY = "sorry";
    private Document homeDoc;
    private Document sensorDoc;
    private Document actuatorDoc;
    private Document vibratorDoc;
    private Document flashLightDoc;
    private Document sorryDoc;

    private Context context;
    private HTMLFactoryListener mListener;
    private String rescueHTML = null;
    private String noSensorValueHTML;
    private Boolean setAddr = false;

    private List<Sensor> mSensors;
    private Sensor currentSensor = null;
    private List<Double> sensorValues;
    private SensorManager mSensorManager;

    private final String LOGTAG = "## VV - HTMLFactory ##";

    HTMLFactory(HTMLFactoryListener listener, Context c, List<Sensor> sensors,SensorManager sensorManager)
    {
        mSensors = sensors;
        context = c;
        mListener = listener;
        mSensorManager = sensorManager;

        // Initialize all html docs
        InputStream homeFile =  context.getResources()
                .openRawResource(context.getResources()
                        .getIdentifier(PARENT, "raw", context.getPackageName()));
        InputStream sensorFile =  context.getResources()
                .openRawResource(context.getResources()
                        .getIdentifier(SENSOR, "raw", context.getPackageName()));
        InputStream actuatorFile =  context.getResources()
                .openRawResource(context.getResources()
                        .getIdentifier(ACTUATOR, "raw", context.getPackageName()));
        InputStream vibratorFile =  context.getResources()
                .openRawResource(context.getResources()
                        .getIdentifier(VIBRATOR, "raw", context.getPackageName()));
        InputStream flashLightFile =  context.getResources()
                .openRawResource(context.getResources()
                        .getIdentifier(FLASHLIGHT, "raw", context.getPackageName()));
        InputStream sorryFile =  context.getResources()
                .openRawResource(context.getResources()
                        .getIdentifier(SORRY, "raw", context.getPackageName()));

        try {
            homeDoc = Jsoup.parse(homeFile, "UTF-8", "");
            sensorDoc = Jsoup.parse(sensorFile, "UTF-8","");
            actuatorDoc = Jsoup.parse(actuatorFile,"UTF-8", "");
            vibratorDoc = Jsoup.parse(vibratorFile,"UTF-8", "");
            flashLightDoc = Jsoup.parse(flashLightFile,"UTF-8", "");
            sorryDoc = Jsoup.parse(sorryFile,"UTF-8", "");

        } catch (IOException e) {e.printStackTrace();}

        // Further initialization
        populateSensorHtml();
        noSensorValueHTML = getNoSensorValueHTML();
        rescueHTML = sorryDoc.toString();
    }


    // Make sure the HTML have correct home address
    private void setHostAddressInHTML()
    {
        String addr;
        if (setAddr == false && (addr = mListener.getHostAddress()) != null) {
            Element h = homeDoc.select("#home").first();
            if (h != null) h.attr("href",addr);
            h = sensorDoc.select("#home").first();
            if (h != null) h.attr("href",addr);
            h = actuatorDoc.select("#home").first();
            if (h != null) h.attr("href",addr);
            h = vibratorDoc.select("#home").first();
            if (h != null) h.attr("href",addr);
            h = flashLightDoc.select("#home").first();
            if (h != null) h.attr("href",addr);
            h = sorryDoc.select("#home").first();
            if (h != null) h.attr("href",addr);

            setAddr = true;
        }
    }

    // Provide a html document when proper one wasnt found
    public String getRescueHTML()
    {
        setHostAddressInHTML();
        String tmp = rescueHTML;
        rescueHTML = sorryDoc.toString();
        return tmp;
    }

    // Get Home page
    public void getParentHTML()
    {
        setHostAddressInHTML();
        rescueHTML = sorryDoc.toString();
        mListener.onHTMLReady(homeDoc.toString());
    }


    /**
     *
     * ACTUATORS stuff
     *
     */

    // Get actuators page
    public void getActuatorHTML(String actuatorName, String args)
    {
        setHostAddressInHTML();
        rescueHTML = sorryDoc.toString();
        if(actuatorName == null) mListener.onHTMLReady(actuatorDoc.toString());
        else if(actuatorName.equals(VIBRATOR)) {
            mListener.onHTMLReady(vibratorDoc.toString());
            if (args != null) vibrate(args);
        } else if(actuatorName.equals(FLASHLIGHT)) {
            mListener.onHTMLReady(flashLightDoc.toString());
            if(args != null) flash(args);
        }

    }

    // Flash the mobile's flash for short period of time
    private void flash(String args)
    {
        Camera mCam = Camera.open();
        Camera.Parameters p = mCam.getParameters();
        boolean fl = args.equals("flash=on");
        if (fl) p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        else p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCam.setParameters(p);
        SurfaceTexture mPreviewTexture = new SurfaceTexture(0);
        try {
            mCam.setPreviewTexture(mPreviewTexture);
        } catch (IOException ex) {
            // Ignore
        }
        mCam.startPreview();
        mCam.release();

    }

    // Handle vibrate logig
    private void vibrate(String args)
    {
        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for time period
        String regexSens = "value=([0-9]*)";
        Pattern patt = Pattern.compile(regexSens);
        Matcher matcher = patt.matcher(args);
        if(matcher.matches())
        {
            int vibtime = Integer.parseInt(matcher.group(1));
            vib.vibrate(20+vibtime*10);
        }

        // Vibrate pattern
        int dot = 200;      // Length of a Morse Code "dot" in milliseconds
        int dash = 500;     // Length of a Morse Code "dash" in milliseconds
        int short_gap = 200;    // Length of Gap Between dots/dashes
        int medium_gap = 500;   // Length of Gap Between Letters
        int long_gap = 1000;    // Length of Gap Between Words

        regexSens = "pattern=([0-2])";
        patt = Pattern.compile(regexSens);
        matcher = patt.matcher(args);
        if(matcher.matches())
        {
            int patternNo =Integer.parseInt(matcher.group(1));
            switch (patternNo) {
                case 0:
                    long[] pattern0 = {0,    dot, short_gap, dot, short_gap, dot,
                            medium_gap,
                            dash, short_gap, dash, short_gap, dash,
                            medium_gap,
                            dot, short_gap, dot, short_gap, dot,
                            long_gap
                    };
                    vib.vibrate(pattern0,-1);
                    break;
                case 1:
                    long[] pattern1 = {0, dot, short_gap,
                            dot, short_gap,dot,
                            short_gap,dot,
                            short_gap,dot,
                            short_gap,
                            dot, short_gap,dot, short_gap,dot, short_gap
                    };
                    vib.vibrate(pattern1,-1);
                    break;
                case 2:
                    long[] pattern2 = {0, dot,short_gap,dash,long_gap,dash,medium_gap,dash,short_gap,dot,short_gap,dot,medium_gap,dash
                    };
                    vib.vibrate(pattern2,-1);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     *
     * SENSOR stuff
     *
     */

    public void getSensortHTML(String sensorName)
    {
        setHostAddressInHTML();
        Log.d(LOGTAG, "getSensorHTML() with " + sensorName);

        if (sensorName == null) {
            // Remove all children
            Element valList = sensorDoc.select(".sensordatalist").first();
            if(valList != null && valList.children() != null) {
                for (Element child : valList.children()) {
                    child.remove();
                }
            }

            sensorName = DEFAULT_SENSOR_NAME;
            Element elem = sensorDoc.select(".sensorname").first();
            elem.text(sensorName);
            mListener.onHTMLReady(sensorDoc.toString());
        } else {
            for (Sensor s: mSensors) {
                if (s.getName().toLowerCase().replaceAll(" ","_").equals(sensorName)){
                    currentSensor = s;
                    mSensorManager.registerListener(this,s,SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
            rescueHTML = noSensorValueHTML;
        }
    }

    // Sensor list will be updated
    public void updateSensorList(List<Sensor> sensors)
    {
        if (sensors != null) {
            mSensors = sensors;
            populateSensorHtml();
        }
    }

    // Put sensors into html document
    private void populateSensorHtml()
    {
        if(mSensors != null)
        {
            Element sensList = sensorDoc.select(".sensorlist").first();
            for(Sensor s : mSensors){
                String name = s.getName();
                sensList.append("<li class='sensor'><a href=/sensors/"
                        +name.replaceAll(" ", "_").toLowerCase()+">" +name+"</a></li>");
            }
        }
    }

    // Generate HTML which explains that sensor is not available
    private String getNoSensorValueHTML()
    {
        setHostAddressInHTML();
        Element valList = sensorDoc.select(".sensordatalist").first();
        // Remove all children
        if(valList != null && valList.children() != null) {
            for (Element child : valList.children()) {
                child.remove();
            }
        }
        if (valList != null) valList.append("<li>No values available at the moment, please try again later.</li>");
        return sensorDoc.toString();
    }

    // Listen to sensor changes
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        Log.d(LOGTAG, "onSensorChanged(), currentSensor: " + currentSensor.getName() + " eventSensor " + event.sensor.getName());
        if(currentSensor != null && event.sensor.getName().equalsIgnoreCase(currentSensor.getName()))
        {
            sensorValues = new ArrayList<>();
            for(int i = 0; i < event.values.length; i++ ) {sensorValues.add(new Double(event.values[i]));}

            Element elem = sensorDoc.select(".sensorname").first();
            Sensor sensor = null;
            if (mSensors != null) {
                for (Sensor s : mSensors){
                    if(s.getName().equals(currentSensor)) sensor = s;
                }
            }
            elem.text(currentSensor.getName());

            // Remove all children
            Element valList = sensorDoc.select(".sensordatalist").first();
            if(valList.children() != null) {
                for (Element child : valList.children()) {
                    child.remove();
                }
            }
            if (sensorValues != null) {
                for (Double d : sensorValues){
                    valList.append("<li>"+d.toString()+"</li>");
                }
            }
        }
        mListener.onHTMLReady(sensorDoc.toString());

        rescueHTML = sorryDoc.toString();
        currentSensor = null;
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

}
