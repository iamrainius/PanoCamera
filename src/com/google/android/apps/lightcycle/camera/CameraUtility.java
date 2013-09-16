package com.google.android.apps.lightcycle.camera;

import java.util.Iterator;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;

import com.google.android.apps.lightcycle.camera.CameraApiProxy.CameraProxy;
import com.google.android.apps.lightcycle.panorama.DeviceManager;
import com.google.android.apps.lightcycle.util.Size;

public class CameraUtility {
	private static final String TAG = CameraUtility.class.getSimpleName();
	private boolean hasBackFacingCamera;
	private Camera.Size photoSize;
	private Size previewSize;
	private float fieldOfView;

	public CameraUtility(int w, int h) {
		CameraProxy proxy = CameraApiProxy.instance().openBackCamera();
		if (proxy == null) {
			hasBackFacingCamera = false;
			previewSize = new Size(0, 0);
			fieldOfView = 0.0f;
			return;
		}

		hasBackFacingCamera = true;
		Camera.Size size = getClosetPreviewSize(proxy, w, h);
		previewSize = new Size(size.width, size.height);
		fieldOfView = DeviceManager.getCameraFieldOfViewDegrees(proxy
				.getParameters().getHorizontalViewAngle());
		proxy.release();
	}

	private android.hardware.Camera.Size getClosetPreviewSize(
			CameraProxy proxy, int w, int h) {
		List<Camera.Size> supportedPreSizes = proxy.getParameters()
				.getSupportedPreviewSizes();

		int size = w * h;
		int maxSize = Integer.MAX_VALUE;
		Camera.Size prevSize = supportedPreSizes.get(0);
		Iterator<Camera.Size> it = supportedPreSizes.iterator();
		while (it.hasNext()) {
			Camera.Size sz = it.next();
			int k = Math.abs(sz.width * sz.height - size);
			if (k >= maxSize) {
				continue;
			}
			maxSize = k;
			prevSize = sz;
		}
		return prevSize;
	}

	public void allocateBuffers(CameraProxy proxy, Size size, int paramInt,
			PreviewCallback callback) {
		proxy.setPreviewCallbackWithBuffer(null);
		int i = (int) Math.ceil(ImageFormat.getBitsPerPixel(proxy
				.getParameters().getPreviewFormat())
				/ 8.0F
				* (size.width * size.height));
		for (int j = 0; j < paramInt; ++j)
			proxy.addCallbackBuffer(new byte[i]);
		proxy.setPreviewCallbackWithBuffer(callback);
	}

	public float getFieldOfView() {
		return fieldOfView;
	}

	public String getFlashMode(CameraProxy proxy) {
		List<String> modes = proxy.getParameters().getSupportedFlashModes();
		if ((modes != null) && (modes.contains("off")))
			return "off";
		return "auto";
	}

	public Size getPreviewSize() {
		return previewSize;
	}
}
