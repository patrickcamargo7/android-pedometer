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
    LineGraphView graph1, graph2;
    int stepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graph1 = new LineGraphView(getApplicationContext(), 1000, Arrays.asList("X", "Y", "Z"));

        LinearLayout linLayout = (LinearLayout) findViewById(R.id.linearLayout);

        linLayout.addView(graph1);
        graph1.setVisibility(View.VISIBLE);

        TextView infoText = new TextView(this);
        linLayout.addView(infoText);
        infoText.setVisibility(View.VISIBLE);

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        SensorEventListener acc = new AccSensorEventListener(infoText);
        sensorManager.registerListener(acc, accSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Setting up the reset button for the graph.
        Button btnReset = new Button(this);
        btnReset.setText("Reset Steps");
        linLayout.addView(btnReset);

        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stepCount = 0;
            }
        });
    }

    class AccSensorEventListener implements SensorEventListener{
        TextView output;

        public AccSensorEventListener(TextView input){
            output = input;
        }

        public void onAccuracyChanged(Sensor s, int i){}

        public void onSensorChanged(SensorEvent se){
            if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                accValues.addPoint(se.values[2]);
                float[] avgValuesZ = {(float)0.0, (float)0.0, accValues.getAvgPointZ()};
                graph1.addPoint(avgValuesZ);
                //double slope = accValues.getSlope();
                if (accValues.sign == -2 && accValues.state == 1){
                    accValues.state = 0;
                    stepCount++;
                }
                //Log.v("slope, steps", String.format("%f, %d", slope, stepCount));
                //output.setText(String.format("Steps: %d%nState: %d", stepCount, accValues.state));
                output.setText(String.format("Steps: %d%n", stepCount));
            }
        }
    }
}