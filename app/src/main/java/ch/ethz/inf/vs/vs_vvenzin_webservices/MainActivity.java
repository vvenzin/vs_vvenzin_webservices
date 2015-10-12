package ch.ethz.inf.vs.vs_vvenzin_webservices;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("#### VV ####", "MainActivity - onCreate()");

        setContentView(R.layout.activity_main);

        Button b = (Button) findViewById(R.id.btn_task1);
        b.setOnClickListener(this);
        b = (Button) findViewById(R.id.btn_task2);
        b.setOnClickListener(this);
        b = (Button) findViewById(R.id.btn_task3);
        b.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("#### VV ####", "MainActivity - onResume()");

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d("#### VV ####", "MainActivity - onPause()");

    }

    public void onClick(View b)
    {
        switch (b.getId()) {
            case R.id.btn_task1:
                Intent i0 = new Intent(this, RESTClientActivity.class);
                this.startActivity(i0);
                break;
            case R.id.btn_task2:
                break;
            case R.id.btn_task3:
                Intent i2 = new Intent(getApplicationContext(), RESTServerActivity.class);
                startActivity(i2);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration conf)
    {
        super.onConfigurationChanged(conf);
    }
}
