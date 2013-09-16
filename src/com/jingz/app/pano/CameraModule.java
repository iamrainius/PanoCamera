package com.jingz.app.pano;

import android.view.View;

public interface CameraModule {
	public void init(CameraActivity activity, View frame, boolean reuseScreenNail);
	public void onOrientationChanged(int orientation);
	public void onResumeBeforeSuper();
	public void onResumeAfterSuper();
	public void onPauseBeforeSuper();
	public void onPauseAfterSuper();
	public void onStop();
}
