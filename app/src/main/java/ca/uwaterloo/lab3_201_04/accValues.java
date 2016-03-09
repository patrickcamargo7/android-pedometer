package ca.uwaterloo.lab3_201_04;

//import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class accValues {
    //initializing point arrays
    static Deque<Float> pointsZ = new ArrayDeque<Float>();
    static Deque<Float> avgPointsZ = new ArrayDeque<Float>();

    static Deque<Float> angles = new ArrayDeque<Float>();
    static Deque<Float> avgAngles = new ArrayDeque<Float>();
    
    static int state = 0;
    static int sign = 0;

    static int stepCount = 0;
    static boolean stepCheckEnabled = true;
    static double stepCountNorth = 0;
    static double stepCountEast = 0;

    // Min, max, and reset amplitudes for the algorithm in m/s^2.
    static double minAmplitude = 0.5;
    static double maxAmplitude = 2.2;
    static double resetAmplitude = -0.1;

    // if state is 0, min amplitude is not reached.
    // if state is 1, min amplitude is reached.
    // if state is 2, max amplitude is exceeded.
    final static int avgAmount = 30; //Average the last n samples from the sensor.
    final static int angleAvgAmount = 35;
    static int amountAdded[] = {0};
    static int anglesAdded[] = {0};

    static void addPoint(float z, char option) {
        Deque<Float> t_list = new ArrayDeque<Float>();
        Deque<Float> t_avg = new ArrayDeque<Float>();
        Deque<Float> t_raw = new ArrayDeque<Float>();
        int t_avgAmount = 0;
        int t_added[] = {0};

        if(option == 'p'){ // for points Z
            t_list = pointsZ;
            t_avg = avgPointsZ;
            t_added = amountAdded;
            t_avgAmount = avgAmount;
        }
        else if(option == 'a') { // for angles
            t_avg = avgAngles;
            t_added = anglesAdded;
            t_avgAmount = angleAvgAmount;
            t_list = angles;
        }

        t_list.addLast(z);
        t_added[0]++;

        float total  = 0;

        if (t_added[0] > t_avgAmount) {
            t_list.removeFirst();
            int loopindex = 0;
            //Log.d("new data smoothing set","-------------------");

            for (Iterator<Float> q = t_list.iterator(); q.hasNext(); loopindex++) {
                float val;
                double avgWeight;
                if(option == 'p'){
                     val = q.next();
                }
                else{
                     val = q.next() - z;
                    avgWeight = 1;
                }
                avgWeight = hammingWeight(loopindex, t_avgAmount);

                total  += avgWeight * val;
                //Log.d("val, weight, total ", String.format("%f, %f, %f", val, avgWeight, total ));
            }

            float tempPoint  ;
            //Log.d("sample values", String.format("%f", tempPoint));
            if(option == 'p'){
                tempPoint =  total /(float) t_avgAmount;
                t_avg.addLast(tempPoint );
                flipState(tempPoint );
            } else {
                tempPoint = total /(float) t_avgAmount;
                tempPoint += z;
                t_avg.addLast(tempPoint);
            }
        }
    }


    // see comment above
    static void flipState(float f) {
        if (f < resetAmplitude) state = 0;
        if (f > maxAmplitude) state = 2;
        if (f > minAmplitude && f < maxAmplitude /*&& state != 2*/) state = 1;
        if (f > 0) sign = 1;
        if (sign == 1 && f < 0) sign = -2;
        else if ( f < 0 ) sign = -1;
    }

    // refer to the wikipedia article on window functions
    static double hammingWeight(int n, int N) {
        return 0.54 - 0.46 * Math.cos(2.0 * 3.14 * (double) n / (double) (N - 1));
    }


    static float getAvgPointZ() {
        if (avgPointsZ.size() != 0) return avgPointsZ.peekLast();
        else return 0;
    }

    static float getAvgAngle(){
        if (avgAngles.size() != 0){
            return avgAngles.peekLast();
        }
        else return 0;
    }
}
