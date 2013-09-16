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
	private boolean mPaused;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		mFrame = (FrameLayout) findViewById(R.id.main_content);

		init();

		mCurrentModule = LightCycleHelper.createPanoramaModule();
		mCurrentModule.init(this, mFrame, true);
		mOrientationListener = new MyOrientationEventListener(this);
	}

	private void init() {
		mControlsBackground = findViewById(R.id.controls);
		mShutterSwitcher = findViewById(R.id.camera_shutter_switcher);
		mShutter = (ShutterButton) findViewById(R.id.shutter_button);
	}
	
	
	
	@Override
	protected void onPause() {
		mPaused = true;
		mOrientationListener.disable();
		mCurrentModule.onPauseBeforeSuper();
		super.onPause();
		mCurrentModule.onPauseAfterSuper();
	}

	@Override
	protected void onResume() {
		mPaused = false;
		mOrientationListener.enable();
		mCurrentModule.onResumeBeforeSuper();
		super.onResume();
		mCurrentModule.onResumeAfterSuper();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mCurrentModule.onStop();
		
		// getStateManager().clearTasks();
	}
	
	

	private void openModule(CameraModule module, boolean canReuse) {
		module.init(this, mFrame, canReuse && canReuseScreenNail());
		mPaused = false;
		module.onResumeBeforeSuper();
		module.onResumeAfterSuper();
	}
	
	private void closeModule(CameraModule module) {
		module.onPauseBeforeSuper();
		module.onPauseAfterSuper();
		mFrame.removeAllViews();
	}

	private boolean canReuseScreenNail() {
		// TODO Auto-generated method stub
		return false;
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

	public void notifyScreenNailChanged() {
		throw new RuntimeException();
	}

	public void setSwipingEnabled(boolean enabled) {
		throw new RuntimeException();
	}
	
	public ShutterButton getShutterButton() {
        return mShutter;
    }

}
