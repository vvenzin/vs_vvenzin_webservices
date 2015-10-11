package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class RESTServerActivity extends AppCompatActivity {

    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private ToggleButton tb;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ServerService.MSG_SET_IP_PORT:
                    Bundle b = msg.getData();
                    setIpAndPortView(b.getString("ip"),msg.arg1);
                    break;
                case ServerService.MSG_START_STOP_SERVER:
                    // Check if button is consistent with server state
                    if ((msg.arg1 == 1) != tb.isChecked()) tb.toggle();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.d("#### VV ####", "Service connected");
            try {
                Message msg = Message.obtain(null, ServerService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("#### VV ####", "Service unexpectedly disconnected");
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("#### VV ####", "RESTServerActivity - onCreate()");
        setContentView(R.layout.activity_restserver);

        /*
        Button sb = (Button) findViewById(R.id.start_service_button);
        sb.setOnClickListener(this);
        sb = (Button)findViewById(R.id.stop_service_button);
        sb.setOnClickListener(this);
        */
        tb = (ToggleButton) findViewById(R.id.start_stop_server);
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startStopServer(isChecked);
            }
        });


        requestServerRunningStatus();

        // Bind to existing service or create new one
        Log.d("#### VV ####", "Service is already running " + Boolean.toString(ServerService.isRunning()));
        bindServerService();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("#### VV ####", "RESTServerActivity - onResume()");

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d("#### VV ####", "RESTServerActivity - onPause()");

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("#### VV ####", "RESTServerActivity - onDestroy()");

    }

    @Override
    public void onConfigurationChanged(Configuration conf)
    {
        super.onConfigurationChanged(conf);
    }

    /*
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case  R.id.start_stop_server:
                tb.toggle();
                break;
        }
    }*/

    private void setIpAndPortView(String ip, int port)
    {
        TextView ipPortTagValue = (TextView) findViewById(R.id.ip_port_value);
        ipPortTagValue.setText(ip + ":" + Integer.toString(port));
    }

    // Bind to ServerService
    private void bindServerService()
    {
        bindService(new Intent(this, ServerService.class), mConnection, getApplicationContext().BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d("#### VV ####", "Bind ServerService");
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
                Log.d("#### VV ####", "Unbind ServerService");
            }
        }
    }

    // startStop == 1 to start sertver , startStop == 0 to start sertver
    // Also requests information if server is running
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

    private void requestServerRunningStatus()
    {
        if (mIsBound && mService != null) {
            try {
                Message msg = Message.obtain(null, ServerService.MSG_START_STOP_SERVER, 0, 1);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {e.printStackTrace();}
        }
    }


    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                /*
                try {

                    Message msg = Message.obtain(null, ServerService.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                }*/
            }
        }
    }

}
