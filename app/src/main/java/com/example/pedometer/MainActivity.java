package com.example.pedometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    int PHYISCAL_ACTIVITY = 1000;
    int STEP_MAX = 8000;
    SensorManager sensorManager;
    TextView tv_Steps;
    CircleProgressBar progressBar;
    private int milestoneStep = 0;

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
        final TextView tv_StepsMax = (TextView) findViewById(R.id.textView_StepsOf);
        tv_StepsMax.setText("из " + String.valueOf(STEP_MAX));
        progressBar.setLongClickable(true);
        progressBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                reset();
                return true;
            }
        });
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
            int totalStepSinceReboot = (int)steps;

            int todayStep = getPreferences(today());
            if(milestoneStep != 0) {
                int additionStep = totalStepSinceReboot - milestoneStep;
                todayStep = todayStep + additionStep;
                savePreferences(today(), todayStep);

                //Log.i("TAG","Your today step now is "+getPreferences(today()));
            }
            milestoneStep = totalStepSinceReboot;

            tv_Steps.setText(String.valueOf(todayStep));
            progressBar.setProgressWithAnimation(todayStep > STEP_MAX ? STEP_MAX : todayStep);
        }
    }

    private void savePreferences(String key, int value) {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private int getPreferences(String key){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);

    }

    public String today() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(Calendar.getInstance().getTime());
    }

    public void reset() {
        savePreferences(today(), 0);
        progressBar.setProgressWithAnimation(0);
        tv_Steps.setText(String.valueOf(0));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Needed this method to skip the error
        // error: MainActivity is not abstract and does not override abstract method onAccuracyChanged(Sensor,int) in SensorEventListener
        // public class MainActivity extends AppCompatActivity implements SensorEventListener
    }
}