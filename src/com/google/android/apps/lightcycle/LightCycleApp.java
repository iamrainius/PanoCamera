package com.google.android.apps.lightcycle;

import com.google.android.apps.lightcycle.camera.CameraUtility;
import com.google.android.apps.lightcycle.panorama.LightCycleNative;
import com.google.android.apps.lightcycle.util.Size;

public class LightCycleApp {
	private static String appVersion = "999";
	private static CameraUtility cameraUtil;

	public static String getAppVersion() {
		return appVersion;
	}

	public static CameraUtility getCameraUtility() {
		return cameraUtil;
	}
	
	public static void initLightCycleNative() {
		cameraUtil = new CameraUtility(320, 240);
		Size size = cameraUtil.getPreviewSize();
		float f = cameraUtil.getFieldOfView();
		LightCycleNative.InitNative(size.width, size.height, f, false);
	}
}
