package com.jingz.app.pano;

import com.jingz.app.pano.util.Util;

import android.view.View;
import android.view.ViewGroup;

public class PanoramaModule implements CameraModule {
	private CameraActivity mActivity;
	private View mRootView;
	private ComboPreferences mPreferences;
	private ViewGroup mContainer;
	private CameraScreenNail mScreenNail;
	
	@Override
	public void init(CameraActivity activity, View frame,
			boolean reuseScreenNail) {
		mActivity = activity;
		mRootView = frame;
		mPreferences = new ComboPreferences(mActivity);
		mActivity.getLayoutInflater().inflate(R.layout.photo_pano_module, (ViewGroup) mRootView);
		mContainer = (ViewGroup) mRootView.findViewById(R.id.camera_app_root);
		// mScreenNail = ((CameraScreenNail)this.mActivity.createCameraScreenNail(true));
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
	public void onResumeBeforeSuper() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResumeAfterSuper() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPauseBeforeSuper() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPauseAfterSuper() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		
	}

}
