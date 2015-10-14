package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Scanner;

public class ServerService extends Service {

    // CONSTANTS
    private final int PORT = 8088;
    private final String PAGE_INDEX = "index";
    private final String HTTP_RESPONSE = "HTTP/1.1 200 OK\r\n\r\n";
    private final String LOGTAG = "## VV-ServerService ##";

    private ServerSocket serverSocket;
    private Thread serverThread;
    private String mAddress;
    private static boolean isRunning = false;
    private boolean serverIsRunning = false;

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

        startForeground(1337, note);
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

    private Runnable thread = new Runnable()
    {
        @Override
        public synchronized void run()
        {
            try {
                serverSocket = new ServerSocket(PORT);
                mAddress = getWlanInterface();
                Log.d(LOGTAG, "Address " + mAddress);
                sendIpAndPort();

                Log.d(LOGTAG,"Listening on port " + PORT + "...");
                while (true)
                {
                    Socket client = serverSocket.accept(); // Blocking -> not busywaiting

                    // In/out streams
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    // Send Home page
                    String page = fileReader(PAGE_INDEX);
                    out.write(HTTP_RESPONSE + page);
                    out.flush();
                    out.close();

                    // TODO: Parse request
                    String line = in.readLine();
                    while (!line.isEmpty()) {
                        Log.d(LOGTAG,line);
                        line = in.readLine();
                    }

                    // TODO: React to request

                }

            } catch (IOException e) {e.printStackTrace();}
        }

    };

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
     * Helper functions for server
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

    // Returns content of html file as a string
    public String fileReader(String fileName) {

        InputStream file = getResources()
                .openRawResource(getResources()
                        .getIdentifier(fileName, "raw", getPackageName()));
        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner;
        scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            text.append(scanner.nextLine() + NL);
        }
        return text.toString();
    }

    // Reads bytestream from client and returns a string
    private String readInput(InputStream is) {

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        try
        {
            String current;
            String header = "";
            while (!(current = in.readLine()).isEmpty())
            {
                header += current + System.getProperty("line.separator");
            }

            return header;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
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

    /* ONLY TEMPLATE fot sending messages
    private void sendMessageToUI(int intvaluetosend) {
        for (int i = mClients.size()-1; i >= 0; i--) {
            try {
                // Send data as an Integer
                //mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));

                //Send data as a String
                Bundle b = new Bundle();
                b.putString("str1", "ab" + intvaluetosend + "cd");
                Message msg = Message.obtain(null, MSG_START_STOP_SERVER);
                msg.setData(b);
                mClients.get(i).send(msg);

            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
    */
}


