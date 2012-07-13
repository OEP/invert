package org.oep.invert;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class GameScreen extends Activity {
    /** Called when the activity is first created. */
	
	private GameWindow gameWindow;
	private SensorManager mSensorManager;
	private Sensor mDefaultAccelerometer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        gameWindow = (GameWindow) findViewById(R.id.main_window);
        
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mDefaultAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(gameWindow, mDefaultAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	gameWindow.die();
    	mSensorManager.unregisterListener(gameWindow);
    }
}