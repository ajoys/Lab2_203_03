package ca.uwaterloo.lab2_203_03;

import java.util.Arrays;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GeneralSensorEventListener implements SensorEventListener{
	private TextView mTvGen;			// General Text View
	private String sensorType;			// Used to know what kind of sensor is being used
	//Max Values
	private float xMaxValueAccel = 0;
	private float yMaxValueAccel = 0;
	private float zMaxValueAccel =0;
	private float mNumOfAverage = 0;
	private float mAverageMax = 0;
	private float mCurrentMax = 0;
	private float mAverageMin = 0;
	private float mCurrentMin = 0;
	private float TESTMIN;
	private float x;
	private float y;
	private float z;
	private float maxAmp = (float)2.0;
	private float minAmp = (float)-0.4;
	private int stepCounter;
	private boolean hasStepped = false;
	// values for the FSM
	private final int mRest = 0;
	private final int mRising = 1;
	private final int mPeak = 2;
	private final int mFalling = 3;
	private final int mNegativeFall = 4;
	private final int mNegativeMin = 5;
	private final int mNegativeRising = 6;
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
		Button resetMax = new Button(context);
		resetMax.setText("RESET");
		layout.addView(resetMax);
		resetMax.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	xMaxValueAccel = 0;
		    	yMaxValueAccel = 0;
		    	zMaxValueAccel =0;
		    	stepCounter = 0;
		    	mNumOfAverage = 0;
		    } 
		});
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		//Accelerometer
		if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
			
			x += (se.values[0] - x)/5;
			y += (se.values[1] - y)/5;
			z += (se.values[2] - z)/5;
			
			// Storing smoothed values into array
			float[] values = new float[]{x, y, z};
			
			//adding points to graph
			graph.addPoint(values);
			
			String value = String.format("\nX:%.4f "
					+ "\nY:%.4f"
					+ "\nZ:%.4f \n", x, y, z);
			
			//updating max values to textView
			String maxValues = String.format("\nX-Max:%.4f "
					+ "\nY-Max:%.4f"
					+ "\nZ-Max:%.4f \n", xMaxValueAccel, yMaxValueAccel, zMaxValueAccel);
			
			switch(currentState){
				case mRest:
					if (z >= (0.1 * maxAmp)){
						currentState = 1;
					}
					else currentState = 0;
					break;
				case mRising:
					if (z >= (0.9 * maxAmp)){
						currentState = 2;
					}
					else if(z <= (0.1 * maxAmp)){
						currentState = 0;
					}
					break;
				case mPeak:
					if (z > mCurrentMax){
						mCurrentMax = z;
					}	
					if (z <= (0.9 * maxAmp)){
						currentState = 3;
					}
					
					break;
				case mFalling:
					if (z <= (maxAmp * 0.1)){
						currentState = 4;
					}
				case mNegativeFall:
					if (z < minAmp * 0.95){
						currentState = 5;
					}
					break;
				case mNegativeMin:
					if (z < mCurrentMin){
						mCurrentMin = z;
					}	
					if (z >= minAmp * 0.95){
						currentState = 6;
					}
					break;
				case mNegativeRising:
					mNumOfAverage++;
					mAverageMax = (mAverageMax + mCurrentMax)/mNumOfAverage ;
					mCurrentMax = 0;
					mAverageMin = (mAverageMin + mCurrentMin)/mNumOfAverage;
					TESTMIN = mCurrentMin;
					mCurrentMin = 0;
					stepCounter++;
		
					if(mNumOfAverage == 5){
						if (mAverageMax > 0.8){
							maxAmp = mAverageMax;
							mAverageMax = 0;
							mNumOfAverage = 0;
						}
						else{
							mAverageMax = 0;
							mNumOfAverage = 0;
						}
						
						if (mAverageMin < 0){
							minAmp = mAverageMin;
							mAverageMin = 0;
						}
			
					}
					currentState = 0;
					break;
			}
			mTvGen.setText("----" + sensorType + "-----" + value + "\n\nSteps: " + stepCounter + "\n Average: " 
					+ mAverageMax + "\n" + maxAmp + "\n" + mNumOfAverage + "\nCurrentState" + currentState + "\nMinAmp " + minAmp
					+ "\nAverage Min" + mAverageMin +"\nCurrent: "  + TESTMIN );
		}
	}

}
