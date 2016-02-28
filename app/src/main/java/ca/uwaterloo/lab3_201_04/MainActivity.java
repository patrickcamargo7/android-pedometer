package ca.uwaterloo.lab3_201_04;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.view.View;

import ca.uwaterloo.sensortoy.LineGraphView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    LineGraphView graph;
    int stepCount = 0;
    float stepCountNorth = 0; // TODO: Use the stepCountNorth/East variables.
    float stepCountEast = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graph = new LineGraphView(getApplicationContext(), 1000, Arrays.asList("X", "Y", "Z"));

        LinearLayout linLayout = (LinearLayout) findViewById(R.id.linearLayout);

        linLayout.addView(graph);
        graph.setVisibility(View.VISIBLE);

        TextView stepText = new TextView(this);
        linLayout.addView(stepText);
        stepText.setVisibility(View.VISIBLE);

        TextView oriText = new TextView(this);
        linLayout.addView(oriText);
        oriText.setVisibility(View.VISIBLE);

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor gravSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener stepListener = new StepSensorEventListener(stepText);
        SensorEventListener orientationListener = new OrientationSensorEventListener(oriText);

        sensorManager.registerListener(stepListener, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(orientationListener, gravSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(orientationListener, magSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Setting up the reset button for the graph.
        Button btnReset = new Button(this);
        btnReset.setText("Clear Displacement");
        linLayout.addView(btnReset);

        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stepCount = 0;
                stepCountNorth = 0;
                stepCountEast = 0;
            }
        });
    }

    class StepSensorEventListener implements SensorEventListener{
        TextView output;

        public StepSensorEventListener(TextView input){
            output = input;
        }

        public void onAccuracyChanged(Sensor s, int i){}

        public void onSensorChanged(SensorEvent se){
            if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                accValues.addPoint(se.values[2]);
            }
            float[] avgValuesZ = {(float)0.0, (float)0.0, accValues.getAvgPointZ()};
            graph.addPoint(avgValuesZ);
            if (accValues.sign == -2 && accValues.state == 1){
                accValues.state = 0;
                stepCount++;
            }
            //Log.v("slope, steps", String.format("%f, %d", slope, stepCount));
            //output.setText(String.format("Steps: %d%nState: %d", stepCount, accValues.state));
            output.setText(String.format("Steps: %d%n", stepCount));
        }
    }

    class OrientationSensorEventListener implements SensorEventListener{
        TextView output;
        float [] rotation = new float[9];
        float [] gravity = new float[3];
        float [] magnetic = new float[3];
        float [] orientation = new float[3];
        double bearing = 0;

        public OrientationSensorEventListener(TextView input){
            output = input;
        }

        public void onAccuracyChanged(Sensor s, int i){}

        public void onSensorChanged(SensorEvent se){
            if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                // TODO: Add grav/acc smoothing here if not smoothing orientation afterwards.
                gravity[0] = se.values[0];
                gravity[1] = se.values[1];
                gravity[2] = se.values[2];
            } else if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // TODO: Add mag smoothing here if not smoothing orientation afterwards.
                magnetic[0] = se.values[0];
                magnetic[1] = se.values[1];
                magnetic[2] = se.values[2];
            }

            SensorManager.getRotationMatrix(rotation, null, gravity, magnetic);
            SensorManager.getOrientation(rotation, orientation);
            bearing = orientation[0];
            bearing = Math.toDegrees(bearing);

            if (bearing < 0) {
                bearing += 360;
            }
        }
    }
}