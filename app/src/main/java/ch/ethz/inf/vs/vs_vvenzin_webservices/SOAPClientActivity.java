package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class SOAPClientActivity extends AppCompatActivity implements ch.ethz.inf.vs.vs_vvenzin_webservices.SensorListener {

    double currentTemperature;
    String debug;
    XmlSensor xmlSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soapclient);

        xmlSensor = new XmlSensor();
        xmlSensor.registerListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_soapclient, menu);
        return true;
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
    public void onReceiveString(String message){
        debug=message;
        TextView debugMessage = (TextView) findViewById(R.id.debug_message);
        debugMessage.setText(debug);
    }

    @Override
    public void onPause() {
        xmlSensor.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        xmlSensor.registerListener(this);
        super.onResume();

    }

    public void onDestroy() {
        xmlSensor.unregisterListener(this);
        super.onDestroy();
    }

    public void onClickTemperature(View view) {
        xmlSensor.getTemperature();
    }
}
