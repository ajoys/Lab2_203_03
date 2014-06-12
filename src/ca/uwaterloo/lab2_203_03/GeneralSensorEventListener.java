package ca.uwaterloo.lab2_203_03;

import java.util.Arrays;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GeneralSensorEventListener implements SensorEventListener{
	private TextView mTvGen;			// General Text View
	private String sensorType;			// Used to know what kind of sensor is being used
	//Max Values
	private float xMaxValueAccel = 0;
	private float yMaxValueAccel = 0;
	private float zMaxValueAccel =0;
	private float x;
	private float y;
	private float z;
	private float maxVal = 0;
	private float minVal = 0;
	private int stepCounter;
	private boolean hasStepped = false;
	// values for the FSM
	private final int mRest = 0;
	private final int mRising = 1;
	private final int mPeak = 2;
	private final int mFalling = 3;
	private final int mNegative = 4;
	//current state instantiated at rest
	private int currentState = 0;
	private float zPrev = 0;
	static LineGraphView graph;
	
	// Constructor calls addTextView and initializes string sensortype. 
	public GeneralSensorEventListener(Context context, String sensorType, LinearLayout layout){
		if (sensorType == "Acceleration"){
			graph = new LineGraphView(context,
					100,
					Arrays.asList("x", "y", "z"));
					layout.addView(graph);
		}
		this.sensorType = sensorType;
		addTextView(context, null, layout);
		stepCounter = 0;
	}
	
	// Creating a text view for the listener. 
	public void addTextView(Context context, String value, LinearLayout layout){
		mTvGen = new TextView(context);
		mTvGen.setText(sensorType + value);
		layout.addView(mTvGen);
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		//Accelerometer
		if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
			
			x += (se.values[0] - x)/3;
			y += (se.values[1] - y)/3;
			z += (se.values[2] - z)/3;
			
			// Storing smoothed values into array
			float[] values = new float[]{x, y, z};
			
			//adding points to graph
			graph.addPoint(values);
			
			//Updating max value for accelerometer
			if (xMaxValueAccel < Math.abs(x)){
				xMaxValueAccel = Math.abs(x);
			}
			if (yMaxValueAccel < Math.abs(y)){
				yMaxValueAccel = Math.abs(y);
			}
			if (zMaxValueAccel < Math.abs(z)){
				zMaxValueAccel = Math.abs(z);
			}
			
			String value = String.format("\nX:%.4f "
					+ "\nY:%.4f"
					+ "\nZ:%.4f \n", x, y, z);
			
			mTvGen.setText("----" + sensorType + "-----" + value + "\n\n Steps: " + stepCounter);
			
			switch(currentState){
				case mRest:
					if (z - zPrev > 0.1){
						stepCounter ++;
						currentState =0;
						zPrev = z;
					}
				case mRising:
					return;
				case mPeak:
					return;
				case mFalling:
					return;
				case mNegative:
					return;
			}
			
			//updating max values to textView
			/*String maxValues = String.format("\nX-Max:%.4f "
					+ "\nY-Max:%.4f"
					+ "\nZ-Max:%.4f \n", xMaxValueAccel, yMaxValueAccel, zMaxValueAccel);*/
			
			
		}
	}

}
