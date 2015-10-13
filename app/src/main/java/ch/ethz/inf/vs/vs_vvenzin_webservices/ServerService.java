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

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ServerService extends Service {

    private final String LOGTAG = "## VV-ServerService ##";

    private ServerSocket serverSocket;
    private Thread serverThread;
    private String mAddress;
    private static boolean isRunning = false;
    private boolean serverIsRunning = false;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.


    // Target we publish for clients to send messages to IncomingHandler.
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


    // CONSTANTS
    private final int PORT = 8088;
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_IP_PORT = 3;
    static final int MSG_START_STOP_SERVER = 4;


    public ServerService() {

    }

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
        Log.d(LOGTAG,"onDestroy()");
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

    // Hello
    private synchronized void startSopServer(boolean startStop)
    {
        if (startStop) {
            if (serverThread == null) {
                serverThread = new Thread(thread);
                serverThread.start();
                Log.d(LOGTAG, "Starting server");
                serverIsRunning = true;
                sendIpAndPort(); // Tell app about ip and port
            } else Log.d(LOGTAG, "Server is already running");
        } else {
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

                while (true)
                {
                    Socket client = serverSocket.accept(); // Blocking -> not busywaiting

                }

            } catch (IOException e) {e.printStackTrace();}
        }

    };

    public static boolean isRunning() {return isRunning;}


    /** ONLY TEMPLATE fot sending messages */
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

}
