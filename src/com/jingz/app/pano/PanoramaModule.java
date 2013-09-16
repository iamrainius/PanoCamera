package com.jingz.app.pano;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Process;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.android.apps.lightcycle.LightCycleApp;
import com.google.android.apps.lightcycle.camera.CameraUtility;
import com.google.android.apps.lightcycle.panorama.DeviceManager;
import com.google.android.apps.lightcycle.panorama.IncrementalAligner;
import com.google.android.apps.lightcycle.panorama.LightCycleView;
import com.google.android.apps.lightcycle.sensor.SensorReader;
import com.jingz.app.pano.storage.LocalSessionStorage;
import com.jingz.app.pano.storage.StorageManager;
import com.jingz.app.pano.storage.StorageManagerFactory;
import com.jingz.app.pano.ui.RotateImageView;
import com.jingz.app.pano.ui.ShutterButton;
import com.jingz.app.pano.ui.ShutterButton.OnShutterButtonListener;
import com.jingz.app.pano.util.Util;

public class PanoramaModule implements CameraModule {
	private CameraActivity mActivity;
	private View mRootView;
	private ComboPreferences mPreferences;
	private ViewGroup mContainer;
	private CameraScreenNail mScreenNail;
	private boolean mIsPaused;
	private ShutterButton mShutterButton;
	private boolean mFullScreen;
	private StorageManager mStorageManager = StorageManagerFactory.getStorageManager();
	private LocalSessionStorage mLocalStorage;
	private LightCycleView mMainView = null;
	private SensorReader mSensorReader;
	private IncrementalAligner mAligner;
	private RotateImageView mUndoButton;
	private int mNumberOfImages;
	private OnClickListener mUndoListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mNumberOfImages <= 0) {
				return;
			}
			
			mMainView.undoLastCapturedPhoto();
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					adjustSwitcherAndSwipe();
				}

			});
		}
	};
	
	private void adjustSwitcherAndSwipe() {
//		if (!mFullScreen) {
//			return;
//		}
//		
//		CameraActivity localCameraActivity;
//		if (mNumberOfImages != 0) {
//			localCameraActivity = mActivity;
//			localCameraActivity.setSwipingEnabled(false);
//			mActivity.hideSwitcher();
//			mShutterButton.setVisibility(View.VISIBLE);
//			mActivity.getOrientationManager().lockOrientation();
//			return;
//		}
		throw new RuntimeException();
	}

	@Override
	public void init(CameraActivity activity, View frame,
			boolean reuseScreenNail) {
		mActivity = activity;
		mRootView = frame;
		mPreferences = new ComboPreferences(mActivity);
		mActivity.getLayoutInflater().inflate(R.layout.photo_pano_module,
				(ViewGroup) mRootView);
		mContainer = (ViewGroup) mRootView.findViewById(R.id.camera_app_root);
		// mScreenNail =
		// ((CameraScreenNail)this.mActivity.createCameraScreenNail(true));
		int w = mRootView.getWidth();
		int h = mRootView.getHeight();
		if (Util.getDisplayRotation(mActivity) % 180 != 0) {
			int k = w;
			w = h;
			h = k;
		}
		mScreenNail.setSize(w, h);
	}

	@Override
	public void onOrientationChanged(int orientation) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onPauseBeforeSuper() {
		mIsPaused = true;
		mShutterButton.setOnShutterButtonListener(null);
		if (mLocalStorage != null) {
			mStorageManager.addSessionData(this.mLocalStorage);
		}
		stopCapture();
		mSensorReader.stop();
		if ((mAligner != null) && (!mAligner.isInterrupted())) {
			mAligner.interrupt();
		}

		mRootView.setKeepScreenOn(false);
		mScreenNail.releaseSurfaceTexture();
	}

	@Override
	public void onPauseAfterSuper() {}

	@Override
	public void onStop() {}

	@Override
	public void onResumeBeforeSuper() {
	}

	@Override
	public void onResumeAfterSuper() {
		mIsPaused = false;
		initButtons();
		mScreenNail.acquireSurfaceTexture();
		mActivity.notifyScreenNailChanged();
		String str = Build.MODEL + " (" + Build.MANUFACTURER + ")";
		if (!DeviceManager.isDeviceSupported()) {
			 displayErrorAndExit("Sorry, your device is not yet supported. Model : " + str);
			return;
		}
		
//		if (!LightCycleApp.getCameraUtility().hasBackFacingCamera())
//	    {
//	      displayErrorAndExit("Sorry, your device does not have a back facing camera");
//	      return;
//	    }
		
		Process.setThreadPriority(-19);
		mRootView.setKeepScreenOn(true);
		mStorageManager.init(mActivity);
		mStorageManager.setPanoramaDestination(Storage.DIRECTORY);
		setDisplayRotation();
		startCapture();
	}

	private void startCapture() {
		mNumberOfImages = 0;
		CameraUtility localCameraUtility = LightCycleApp.getCameraUtility();
		
	}
	
	private void stopCapture() {

	}

	private void setDisplayRotation() {
		// TODO Auto-generated method stub
		
	}

	private void initButtons() {
		if (mUndoButton != null) {
			mContainer.removeView(mUndoButton);
			mActivity.getLayoutInflater().inflate(R.layout.photo_pano_undo, mContainer);
		}
		
		mUndoButton = (RotateImageView) mContainer.findViewById(R.id.undo_button);
		mUndoButton.enableFilter(false);
		mUndoButton.setOnClickListener(mUndoListener );
		mShutterButton = mActivity.getShutterButton();
		mShutterButton.setImageResource(R.drawable.btn_shutter_recording);
		mShutterButton.setOnShutterButtonListener(new OnShutterButtonListener() {
			
			@Override
			public void onShutterButtonFocus(boolean pressed) {}
			
			@Override
			public void onShutterButtonClick() {
				onDoneButtonPressed();
			}

		});
	}

	private void displayErrorAndExit(String error) {
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(
				this.mActivity);
		localBuilder.setMessage(error).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface paramDialogInterface,
							int paramInt) {
						mActivity.finish();
					}
				});
		localBuilder.create().show();
	}

	private void onDoneButtonPressed() {
		// TODO Auto-generated method stub
		
	}
	
}
