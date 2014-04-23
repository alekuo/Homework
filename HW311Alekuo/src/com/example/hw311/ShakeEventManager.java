package com.example.hw311;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeEventManager implements SensorEventListener {
	public static interface ShakeListener {
		public void onShake();
	}

	SensorManager sManager;
	Sensor s;
	float gravity[];
	int ALPHA = 10;
	int MOV_THRESHOLD = 10;
	int MOV_COUNTS = 3;
	long firstMovTime;
	long SHAKE_WINDOW_TIME_INTERVAL = 100;
	int counter = 0;
	ShakeListener listener;
	
	public void init(Context ctx) {
	    sManager = (SensorManager)  ctx.getSystemService(Context.SENSOR_SERVICE);
	    s = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    register();
	}

	public void register() {
	    sManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private float calcMaxAcceleration(SensorEvent event) {
		gravity[0] = calcGravityForce(event.values[0], 0);
		gravity[1] = calcGravityForce(event.values[1], 1);
		gravity[2] = calcGravityForce(event.values[2], 2);
		
		float accX = event.values[0] - gravity[0];
		float accY = event.values[1] - gravity[1];
		float accZ = event.values[2] - gravity[2];
		
		float max1 = Math.max(accX, accY);
		return Math.max(max1, accZ);
		}
	
	// Low pass filter
	private float calcGravityForce(float currentVal, int index) {
	    return  ALPHA * gravity[index] + (1 - ALPHA) * currentVal;
	}	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
	    float maxAcc = calcMaxAcceleration(sensorEvent);
	    Log.d("SwA", "Max Acc ["+maxAcc+"]");
	    if (maxAcc >= MOV_THRESHOLD) {
	        if (counter == 0) {
	            counter++;
	            firstMovTime = System.currentTimeMillis();
	            Log.d("SwA", "First move..");
	        } else {
	            long now = System.currentTimeMillis();
	            if ((now - firstMovTime) < SHAKE_WINDOW_TIME_INTERVAL)
	                counter++;
	            else {
	                resetAllData();
	                counter++;
	                return;
	            }
	            Log.d("SwA", "Mov counter ["+counter+"]");
	 
	            if (counter >= MOV_COUNTS)
	                if (listener != null)
	                    listener.onShake();
	        }
	    }
	    listener.onShake();
	}

	private void resetAllData(){
		counter = 0;
	}

}


