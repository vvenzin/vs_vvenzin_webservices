package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class RESTClientActivity extends AppCompatActivity implements SensorListener {

    RawHttpSensor rawHttpSensor;
    HtmlSensor htmlSensor;
    double currentTemperature;
    String debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restclient);

        rawHttpSensor = new RawHttpSensor();
        rawHttpSensor.registerListener(this);

        htmlSensor = new HtmlSensor();
        htmlSensor.registerListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restclient, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceiveDouble(double value) {
        currentTemperature = value;
        TextView showTemperature = (TextView) findViewById(R.id.show_temperature);
        showTemperature.setText(String.valueOf(currentTemperature));
    }

    @Override
    public void onReceiveString(String message) {
        debug = message;
        TextView debugMessage = (TextView) findViewById(R.id.debug_message);
        debugMessage.setText(debug);
    }

    @Override
    public void onPause() {
        rawHttpSensor.unregisterListener(this);
        htmlSensor.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        rawHttpSensor.registerListener(this);
        htmlSensor.unregisterListener(this);
        super.onResume();

    }

    public void onDestroy() {
        rawHttpSensor.unregisterListener(this);
        htmlSensor.unregisterListener(this);
        super.onDestroy();
    }

    public void onClickTemperatureRaw(View view) {
        rawHttpSensor.getTemperature();
    }

    public void onClickTemperatureLib(View view) {
        htmlSensor.getTemperature();
    }
}
