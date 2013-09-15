package com.jingz.app.pano;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;

import com.jingz.app.pano.ui.ShutterButton;
import com.jingz.app.pano.util.LightCycleHelper;

public class CameraActivity extends Activity {

	private FrameLayout mFrame;
	private CameraModule mCurrentModule;
	private int mLastRawOrientation;
	private MyOrientationEventListener mOrientationListener;
	private View mControlsBackground;
	private View mShutterSwitcher;
	private ShutterButton mShutter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		mFrame = (FrameLayout) findViewById(R.id.main_content);

		init();

//		mCurrentModule = LightCycleHelper.createPanoramaModule();
//		mCurrentModule.init(this, mFrame, true);
//		mOrientationListener = new MyOrientationEventListener(this);
	}

	private void init() {
		mControlsBackground = findViewById(R.id.controls);
		mShutterSwitcher = findViewById(R.id.camera_shutter_switcher);
		mShutter = (ShutterButton) findViewById(R.id.shutter_button);
	}

	private class MyOrientationEventListener extends OrientationEventListener {
		public MyOrientationEventListener(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			// We keep the last known orientation. So if the user first orient
			// the camera then point the camera to floor or sky, we still have
			// the correct orientation.
			if (orientation == ORIENTATION_UNKNOWN)
				return;
			mLastRawOrientation = orientation;
			mCurrentModule.onOrientationChanged(orientation);
		}
	}

}
