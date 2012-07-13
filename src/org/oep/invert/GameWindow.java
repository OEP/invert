package org.oep.invert;

import org.oep.invert.Figure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GameWindow extends View implements OnTouchListener, SensorEventListener {
	
	private Figure dood = new Figure(11, 50, 50);
	private boolean mContinue = true;
	
	private float mGX, mGY, mGZ;
	
	private Sensor mAccelerometer;
	
	private Rect mBounds;
	
	private RefreshHandler mRedrawHandler = new RefreshHandler();
	
	class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			GameWindow.this.update();
			GameWindow.this.invalidate(); // Mark the view as 'dirty'
		}
		
		public void sleep(long delay) {
			this.removeMessages(0);
			this.sendMessageDelayed(obtainMessage(0), delay);
		}
	}

    public GameWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameWindow();
   }

    public GameWindow(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	initGameWindow();
    }
	
	private void initGameWindow() {
		this.setOnTouchListener(this);
		
		//SensorManager.get
		
		FigureFrame f1 = new FigureFrame();
		f1.addPoint(0, -10);	// 1
		f1.addPoint(0, 0);		// 2
		f1.addPoint(0, 10);		// 3
		f1.addPoint(-20, 5);	// 4
		f1.addPoint(-10, 0);	// 5
		f1.addPoint(10, 0);		// 6
		f1.addPoint(20, -5);	// 7
		f1.addPoint(-20, 15);	// 8
		f1.addPoint(-10, 20);	// 9
		f1.addPoint(10, 20);	// 10
		f1.addPoint(5, 30);		// 11
		
		FigureFrame f2 = new FigureFrame();
		f2.addPoint(0, -10);	// 1
		f2.addPoint(0, 0);		// 2
		f2.addPoint(0, 10);		// 3
		f2.addPoint(5, 10);		// 4
		f2.addPoint(-5, 5);		// 5
		f2.addPoint(5, 10);		// 6
		f2.addPoint(15, 5);		// 7
		f2.addPoint(-5, 15);	// 8
		f2.addPoint(5, 20);	// 9
		f2.addPoint(-5, 20);		// 10
		f2.addPoint(-10, 30);		// 11
		
		FigureFrame f3 = new FigureFrame();
		f3.addPoint(0, -10);	// 1
		f3.addPoint(0, 0);		// 2
		f3.addPoint(0, 10);		// 3
		f3.addPoint(20, -5);	// 7
		f3.addPoint(10, 0);		// 6
		f3.addPoint(-10, 0);	// 5
		f3.addPoint(-20, 5);	// 4
		f3.addPoint(5, 30);		// 11
		f3.addPoint(10, 20);	// 10
		f3.addPoint(-10, 20);	// 9
		f3.addPoint(-20, 15);	// 8
		
		FigureFrame f4 = new FigureFrame();
		f4.addPoint(0, -10);	// 1
		f4.addPoint(0, 0);		// 2
		f4.addPoint(0, 10);		// 3
		f4.addPoint(5, 10);		// 4
		f4.addPoint(-5, 5);		// 5
		f4.addPoint(5, 10);		// 6
		f4.addPoint(15, 5);		// 7
		f4.addPoint(-10, 30);	// 8
		f4.addPoint(-5, 20);	// 9
		f4.addPoint(5, 10);		// 10
		f4.addPoint(0, 20);		// 11

		
		FigureAnimation animation = new FigureAnimation();
		animation.addFrame(f1);
		animation.addFrame(f2);
		animation.addFrame(f3);
		animation.addFrame(f4);
		
		dood.addEdge(0, 1); // Head to chest
		dood.addEdge(1, 2); // chest to groin
		dood.addEdge(1, 4); // chest to elbow
		dood.addEdge(1, 5); // chest to elbow
		dood.addEdge(4, 3); // elbow to hand
		dood.addEdge(5, 6); // elbow to hand
		dood.addEdge(2, 8); // groin to knee
		dood.addEdge(2, 9); // groin to knee
		dood.addEdge(8, 7); // knee to foot
		dood.addEdge(9, 10); // knee to foot
		
		
		dood.addAnimation("run", animation);
		dood.playAnimation("run", 10, true);
		
		update();
	}
	
	public void update() {
		//if(mBounds == null && (getWidth() != 0 && getHeight() != 0)) {
		mBounds = new Rect(0, 0, getWidth(), getHeight());
		//}
		
		dood.nextFrame();
		dood.move(mBounds);
		dood.accelerate(mGX, mGY);
		{
			double gravity = 0;
			
			if(mGX != 0 && mGY != 0) {
				gravity = Math.atan(mGY / mGX);
				
				if(gravity < 0 && mGX < 0) {
					gravity += Math.PI;
				}
				else if(gravity > 0 && mGX < 0 && mGY < 0) {
					gravity += Math.PI;
				}
			}
			else if(mGX == 0 && mGY != 0)
				gravity = (mGY > 0) ? Math.PI / 2 : 3 * Math.PI / 2;
			else if(mGY == 0 && mGX != 0) //
				gravity = (mGX > 0) ? 0 : Math.PI; 
			
			dood.setOrientationInDegrees((90 - (gravity * 180 / Math.PI)));
		}
		
		if(mContinue) {
			mRedrawHandler.sleep(41);
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		dood.paint(canvas, p);
		
		canvas.drawText(dood.toString(), 0, 20, p);
	}

	public void die() {
		mContinue = false;
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		dood.setPosition(getWidth() / 2, getHeight() / 2);
		dood.setVelocity(0,0);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		mGX = event.values[0];
		mGY = event.values[1];
		mGZ = event.values[2];
	}
}
