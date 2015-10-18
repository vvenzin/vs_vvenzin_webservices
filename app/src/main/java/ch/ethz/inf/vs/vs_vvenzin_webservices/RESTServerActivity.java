package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class RESTServerActivity extends AppCompatActivity {

    private final String LOGTAG = "## VV-ServerActivity ##";

    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private ToggleButton tb;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            /**
             * All message handling goes in here
             * */
            switch (msg.what) {
                case ServerService.MSG_SET_IP_PORT:
                    Bundle b = msg.getData();
                    setIpAndPortView(b.getString("ip"),msg.arg1);
                    break;
                case ServerService.MSG_START_STOP_SERVER:
                    // Check if button is consistent with server state
                    if ((msg.arg1 == 1) != tb.isChecked()) tb.toggle();
                    if (msg.arg1 == 0) setIpAndPortView(null,0); // Clear ip:port in UI
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("#### VV ####", "RESTServerActivity - onCreate()");
        setContentView(R.layout.activity_restserver);

        tb = (ToggleButton) findViewById(R.id.start_stop_server);
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startStopServer(isChecked); // Start/Stop server
            }
        });

        initConnection(); // Connection used for service
        startServerService(); // Bind to existing service or create and start new one
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(LOGTAG, "onResume()");

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(LOGTAG, "onPause()");

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(LOGTAG, "onDestroy()");
        unbindServerService(); // This does NOT stop the service
        // Stop service if server not running
        if(!tb.isChecked()) stopService(new Intent(this,ServerService.class));
    }

    @Override
    public void onConfigurationChanged(Configuration conf) {super.onConfigurationChanged(conf);}

    // Displays ip address and port in UI. Sets default if ip == null
    private void setIpAndPortView(String ip, int port)
    {
        TextView ipPortTagValue = (TextView) findViewById(R.id.ip_port_value);
        if (ip != null)  ipPortTagValue.setText(ip + ":" + Integer.toString(port));
        else ipPortTagValue.setText(getString(R.string.ip_port_value) + ":" + getString(R.string.ip_port_value));
    }

    // Bind to ServerService and start if not already running
    private void startServerService()
    {
        bindService(new Intent(this, ServerService.class), mConnection, getApplicationContext().BIND_AUTO_CREATE);
        mIsBound = true;
        if (ServerService.isRunning()) Log.d(LOGTAG,"Server is already running - bind");
        else {
            startService(new Intent(this,ServerService.class));
            Log.d(LOGTAG, "Service is not running yet bind and start");
        }
    }

    // Unbind from ServerService
    private void unbindServerService()
    {
        if (mIsBound) {
            if(mService != null) {
                try {
                    Message msg = Message.obtain(null, ServerService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {e.printStackTrace();}
                unbindService(mConnection);
                mIsBound = false;
                Log.d(LOGTAG, "Unbind ServerService");
            }
        }
    }

    // startStop == 1 to start server , startStop == 0 to stop server
    // Also atomatically requests information if server is running -> causes button update
    private void startStopServer(boolean startStop)
    {
        int s = 0;
        if (startStop) s = 1;
        if (mIsBound && mService != null) {
            try {
                Message msg = Message.obtain(null, ServerService.MSG_START_STOP_SERVER, s, 1);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {e.printStackTrace();}
        }
    }

    // Causes button to update on response
    private void requestServerRunningStatus()
    {
        if (mIsBound && mService != null) {
            try {
                Message msg = Message.obtain(null, ServerService.MSG_START_STOP_SERVER, -1, 1);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {e.printStackTrace();}
        }
    }

    // Helper function
    private void initConnection()
    {
        mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.d(LOGTAG, "Service connected");
            try {
                Message msg = Message.obtain(null, ServerService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {e.printStackTrace();}
            requestServerRunningStatus();
        }

        public void onServiceDisconnected(ComponentName className)
        {
            Log.d(LOGTAG, "Service unexpectedly disconnected");
            mService = null;
        }
        };
    }

}
