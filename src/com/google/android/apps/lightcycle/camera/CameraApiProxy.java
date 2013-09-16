package com.google.android.apps.lightcycle.camera;

import java.io.IOException;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.Log;
import android.view.SurfaceHolder;

public abstract class CameraApiProxy {
	private static final String TAG = CameraApiProxy.class.getSimpleName();
	private static CameraApiProxy instance;
	
	public static CameraApiProxy instance() {
		if (instance == null) {
			Log.e(TAG, "No CameraApiProxy implementation set. Use CameraApiProxy.setActiveProxy first.");
		}
		return instance;
	}
	
	public static void setActiveProxy(CameraApiProxy proxy) {
		instance = proxy;
	}
	
	public abstract CameraProxy openBackCamera();
	
	public static abstract interface CameraProxy {
		void addCallbackBuffer(byte[] buffer);
		void enableShutterSound(boolean enabled);
		Parameters getParameters();
		void release();
		void setDisplayOrientation(int orient);
		void setParameters(Parameters params);
		void setPreviewCallback(PreviewCallback callback);
		void setPreviewCallbackWithBuffer(PreviewCallback callback);
		void setPreviewDisplay(SurfaceHolder holder) throws IOException;
		void setPreviewTexture(SurfaceTexture texture) throws IOException;
		void startPreview();
		void stopPreview();
		void takePicture(ShutterCallback shutterCallback, PictureCallback picCallback1, PictureCallback picCallback2);
	}
}
