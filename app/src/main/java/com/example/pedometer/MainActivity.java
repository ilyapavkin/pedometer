package com.example.pedometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    int PHYISCAL_ACTIVITY = 1000;
    int STEP_MAX = 8000;
    SensorManager sensorManager;
    TextView tv_Steps;
    CircleProgressBar progressBar;

    boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        tv_Steps = (TextView) findViewById(R.id.textView_Steps);
        progressBar = (CircleProgressBar) findViewById(R.id.custom_progressBar);

        progressBar.setMax(STEP_MAX);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        isRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null)
        {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else
        {
            Toast.makeText(this, "STEP_COUNTER sensor not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        isRunning = false;
//    sensorManager.unregisterListener(this); // this will stop detecting steps!
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (isRunning)
        {
            float steps = event.values[0];
            tv_Steps.setText(String.valueOf((int)steps));
            progressBar.setProgressWithAnimation(steps > STEP_MAX ? STEP_MAX : steps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Needed this method to skip the error
        // error: MainActivity is not abstract and does not override abstract method onAccuracyChanged(Sensor,int) in SensorEventListener
        // public class MainActivity extends AppCompatActivity implements SensorEventListener
    }
}