package ca.uwaterloo.lab3_201_04;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.view.View;

import ca.uwaterloo.sensortoy.LineGraphView;
import ca.uwaterloo.mapper.*;

import java.util.Arrays;
import java.lang.Math;

public class MainActivity extends AppCompatActivity {
    LineGraphView graph;
    Mapper mv;
    PedometerMap map;
    MapLoader ml = new MapLoader();

    // Using a global variable for the bearing because we need to access it from the orientation listener and the step counter.
    double bearingRadian = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graph = new LineGraphView(getApplicationContext(), 1000, Arrays.asList("X", "Y", "Z"));

        mv = new Mapper(getApplicationContext(), 1440, 1000, 45, 45);
        registerForContextMenu(mv);
        map = ml.loadMap(getExternalFilesDir(null), "E2-3344.svg");
        mv.setMap(map);

        // Frame layout for the map/graph and linear layout for the text and buttons underneath.
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.FrameLayout);
        LinearLayout linLayout = (LinearLayout) findViewById(R.id.linearLayout);


        frameLayout.addView(graph);
        graph.setVisibility(View.INVISIBLE);

        frameLayout.addView(mv);
        mv.setVisibility(View.VISIBLE);

        // Creating the TextView to display the steps taken.
        TextView stepText = new TextView(this);
        linLayout.addView(stepText);
        stepText.setVisibility(View.VISIBLE);

        // Creating the TextView to display the bearing.
        TextView oriText = new TextView(this);
        linLayout.addView(oriText);
        oriText.setVisibility(View.VISIBLE);

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor rotSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        SensorEventListener stepListener = new StepSensorEventListener(stepText);
        SensorEventListener orientationListener = new OrientationSensorEventListener(oriText);

        sensorManager.registerListener(stepListener, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(orientationListener, rotSensor, SensorManager.SENSOR_DELAY_NORMAL);

        final Button btnReset = new Button(this);
        btnReset.setText("Clear Displacement");
        linLayout.addView(btnReset);

        final Button btnCalib = new Button(this);
        btnCalib.setText("Calibration");
        linLayout.addView(btnCalib);

        final Button btnSwitch = new Button(this);
        btnSwitch.setText("Switch to Graph");
        linLayout.addView(btnSwitch);

        // Popup calibration instructions and pause step counting when calibration button is pressed.
        btnCalib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                accValues.stepCheckEnabled = false;
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                OrientationDialogFragment nf = new OrientationDialogFragment();
                nf.show(ft, "show");
            }
        });

        // Set all step values to zero when clear displacement button is pressed.
        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                accValues.stepCount = 0;
                accValues.stepCountNorth = 0;
                accValues.stepCountEast = 0;
            }
        });

        // Switch between map of room and graph of Z acceleration when switch button is pressed.
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(graph.getVisibility() == View.VISIBLE) {
                    graph.setVisibility(View.INVISIBLE);
                    mv.setVisibility(View.VISIBLE);
                    btnSwitch.setText("Switch to graph");
                } else {
                    graph.setVisibility(View.VISIBLE);
                    mv.setVisibility(View.INVISIBLE);
                    btnSwitch.setText("Switch to Map");
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mv.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item) || mv.onContextItemSelected(item);
    }

    class StepSensorEventListener implements SensorEventListener{
        TextView output;

        public StepSensorEventListener(TextView input){
            output = input;
        }

        public void onAccuracyChanged(Sensor s, int i){}

        public void onSensorChanged(SensorEvent se){
            if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                accValues.addPoint(se.values[2]); // Give values to be averaged to the processing class.
            }

            float[] avgValuesZ = {(float)0.0, (float)0.0, accValues.getAvgPointZ()};
            graph.addPoint(avgValuesZ); // Graph the average values.

            // Count a new step if certain algorithm conditions are met.
            if (accValues.sign == -2 && accValues.state == 1 && accValues.stepCheckEnabled){
                accValues.state = 0;
                accValues.stepCount++;
                // Ensures sin and cos calculate from the same angle.
                double tempBearing = bearingRadian;
                // North and East components of each step are the cos and sin of the bearing when the step is taken, respectively.
                accValues.stepCountNorth += Math.cos(tempBearing);
                accValues.stepCountEast += Math.sin(tempBearing);
            }

            output.setText(String.format("Steps: %d%n", accValues.stepCount));
        }
    }

    class OrientationSensorEventListener implements SensorEventListener{
        TextView output;
        float [] rotation = new float[16];
        float [] orientation = new float[3];
        double bearingDegree = 0;

        public OrientationSensorEventListener(TextView input){
            output = input;
        }

        public void onAccuracyChanged(Sensor s, int i){}

        public void onSensorChanged(SensorEvent se){
            if(se.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                SensorManager.getRotationMatrixFromVector(rotation , se.values);
                SensorManager.getOrientation(rotation, orientation);
            }

            SensorManager.getOrientation(rotation, orientation);
            bearingRadian = orientation[0];
            bearingDegree = Math.toDegrees(bearingRadian);

            // If the bearing is negative, add 360 to make sure the bearing is always positive.
            if(bearingDegree < 0) {
                bearingDegree += 360;
            }

            output.setText(String.format("Bearing: %.3f degrees%nSteps North: %.3f steps%nSteps East: %.3f steps%n", bearingDegree, accValues.stepCountNorth, accValues.stepCountEast));
        }
    }
}