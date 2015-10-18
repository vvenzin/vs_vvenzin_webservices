package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ServerService extends Service implements HTMLFactoryListener {

    // CONSTANTS
    private final int PORT = 8088;
    //private final String PAGE_INDEX = "index";
    //private final String HTTP_RESPONSE = "HTTP/1.1 200 OK\r\n\r\n";
    private final String LOGTAG = "## VV-ServerService ##";

    private ServerSocket serverSocket;
    private Thread serverThread;
    private String mAddress;
    private static boolean isRunning = false;
    private boolean serverIsRunning = false;
    private HTMLFactory htmlFactory;

    /**
     * Message handling
     */

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_IP_PORT = 3;
    static final int MSG_START_STOP_SERVER = 4;

    // Target we publish for clients to send messages to IncomingHandler.
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo); // Add caller
                    sendIpAndPort();
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo); // Remove caller
                    break;
                case MSG_START_STOP_SERVER:
                    if (msg.arg1 > -1) startSopServer(msg.arg1 == 1);
                    if (msg.arg2 == 1) sendServerState();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    public ServerService() {

    }

    /**
     * Callbacks
     */

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(LOGTAG, "onCreate()");
        mAddress = getWlanInterface();

        //get a list with all sensors
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> listSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
        htmlFactory = new HTMLFactory(this,getApplicationContext(),listSensor,sensorManager);

        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(LOGTAG, "onStartCommand()");

        Intent i = new Intent(this, RESTServerActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);
        Notification note = new Notification.Builder(getApplicationContext())
                .setContentText("Test Notification")
                .setContentIntent(pi)
                .build();

        note.flags|=Notification.FLAG_NO_CLEAR;

        startForeground(1337, note); // We want our service to continue as long as possible
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(LOGTAG, "onDestroy()");
        Log.d(LOGTAG, "onDestroy()");
        if (serverSocket != null) {
            try {
                serverSocket.close();
                startSopServer(false);
            } catch (IOException e) {e.printStackTrace();}
        }
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {return mMessenger.getBinder();}



    /**
     * SERVER
     */

    private String requestedHTML = null;

    @Override
    public String getHostAddress()
    {
        if (mAddress != null) return "http://" + mAddress+ ":"+Integer.toString(PORT);
        else return null;
    }

    private Runnable thread = new Runnable()
    {
        @Override
        public synchronized void run()
        {
            try {
                serverSocket = new ServerSocket(PORT);
                Log.d(LOGTAG, "Address " + mAddress);
                sendIpAndPort();

                Log.d(LOGTAG,"Listening on port " + PORT + "...");
                while (true)
                {
                    Socket client = serverSocket.accept(); // Blocking -> not busywaiting

                    // In/out streams
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    String request = "";
                    String line = in.readLine();
                    request += line;
                    while (!line.isEmpty()) {
                        line = in.readLine();
                        request += line;
                    }

                    // Wait for HTML to arrive
                    getHTML(parseGET(request));
                    int waitfor = 50; // 50 seconds
                    while (requestedHTML == null && waitfor > 0) {
                        waitfor--;
                        Log.d(LOGTAG, "Waiting for HTML");
                        try {
                            wait(100);
                        } catch (InterruptedException e) {e.printStackTrace();}
                    }
                    if (waitfor == 0) {
                        Log.d(LOGTAG, "Didnt get html");
                        String rescue = htmlFactory.getRescueHTML(); // Request rescue html
                        if (rescue != null) out.write(rescue);
                        else {
                            // Didnt get any html
                            String notfound = "HTTP/1.1 404 Not Found\r\n\r\n";
                            out.write(notfound);
                        }
                    } else  out.write(requestedHTML);

                    requestedHTML = null;
                    out.flush();
                    out.close();
                    in.close();
                }

            } catch (IOException e) {e.printStackTrace();}
        }

    };

    // Returns ip address form wlan interface
    private String getWlanInterface()
    {
        NetworkInterface wlanInterface = null;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface interf : interfaces)
            {
                if (interf != null && interf.getName().equals("wlan0")) wlanInterface = interf;
            }
            if (wlanInterface != null) {
                for (Enumeration<InetAddress> enumIpAddr = wlanInterface.getInetAddresses();
                     enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress.getAddress().length == 4) {
                        return inetAddress.getHostAddress();
                    }
                }
            } else Log.d(LOGTAG,"Couldnt get wlan interface.");
        } catch (SocketException e) { e.printStackTrace();}
        return null;
    }


    /**
     *
     * Helper functions for server
     *
     */

    private synchronized void startSopServer(boolean startStop)
    {
        if (startStop) {
            // Starting
            if (mAddress != null) {
                if (serverThread == null) {
                    serverThread = new Thread(thread);
                    serverThread.start();
                    Log.d(LOGTAG, "Starting server");
                    serverIsRunning = true;
                    sendIpAndPort(); // Tell app about ip and port
                } else Log.d(LOGTAG, "Server is already running");
            } else Log.d(LOGTAG, "Address was null, didnt start server");
        } else {
            // Stopping
            if (serverThread != null) {
                Thread t = serverThread;
                serverThread = null;
                t.interrupt();
                Log.d(LOGTAG, "Stopping server");
                serverIsRunning = false;

                // Close socket
                try {
                    serverSocket.close();
                } catch (IOException e) {e.printStackTrace();}
            } else Log.d(LOGTAG, "Server is already stopped");
        }
    }


    // Extract requested path from get request
    private String parseGET(String request)
    {
        String parsed = "invalid";
        String regex = "GET(.*)(HTTP.*)";
        Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(request);
        if (matcher.matches()) parsed = matcher.group(1);
        Log.d(LOGTAG,"Paresed following GET request " + parsed);
        return parsed.substring(1,parsed.length()-1);
    }


    // Decide which html page we want based on the request then ask for it
    private void getHTML(String request)
    {
        final String SENSOR = "/sensors";
        final String ACTUATORS = "/actuators";

        String regexSens = SENSOR + "/(.*)";
        String regexAct = ACTUATORS + "/(.*)";
        Pattern patt = Pattern.compile(regexSens);
        Matcher matcher = patt.matcher(request);

        // Home
        if (request.equals("/")) {htmlFactory.getParentHTML();}

        // Sensor - no sensor selected yet
        if (request.equals(SENSOR)) {htmlFactory.getSensortHTML(null);}

        // Sensor
        if (matcher.matches()) {
            String sensorName = matcher.group(1);
            htmlFactory.getSensortHTML(sensorName);
        }

        patt = Pattern.compile(regexAct);
        matcher = patt.matcher(request);

        // Actuator
        if (request.equals(ACTUATORS)) {htmlFactory.getActuatorHTML(null,null);}
        else if (matcher.matches()) {
            String tmp = matcher.group(1);
            String[] parts = tmp.split(Pattern.quote("?"));
            if (parts.length == 1) htmlFactory.getActuatorHTML(parts[0],null); // No actuator selected
            else htmlFactory.getActuatorHTML(parts[0],parts[1]); // Actuator selected
        }
    }


    @Override
    // This function gets called when a requested html file is ready.
    public void onHTMLReady(String html)
    {
        Log.d(LOGTAG,"received HTML");
        requestedHTML = html;
    }


    /**
     * Service client communication
     */

    public static boolean isRunning() {return isRunning;}

    // Sends ip address and port to Activity
    private void sendIpAndPort() {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Bundle b = new Bundle();
                b.putString("ip", mAddress);
                Message msg = Message.obtain(null,MSG_SET_IP_PORT);
                msg.setData(b);
                msg.arg1 = PORT;
                mClients.get(i).send(msg);
            } catch (RemoteException e) {e.printStackTrace();}
        }
    }

    // Sends message with information whether server is running or not
    private void sendServerState()
    {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message msg = Message.obtain(null,MSG_START_STOP_SERVER);
                int r = 0;
                if (serverIsRunning) r = 1;
                msg.arg1 = r;
                mClients.get(i).send(msg);
            } catch (RemoteException e) {e.printStackTrace();}
        }
    }
}


