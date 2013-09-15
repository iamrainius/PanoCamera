package com.jingz.app.pano.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ShutterButton extends ImageView {
	
	private boolean mTouchEnabled = true;
	
	public interface OnShutterButtonListener {
		void onShutterButtonFocus(boolean pressed);
		void onShutterButtonClick();
	}
	
	private OnShutterButtonListener mListener;
	private boolean mOldPressed;

	public ShutterButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnShutterButtonListener(OnShutterButtonListener listener) {
		mListener = listener;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mTouchEnabled) {
			return super.dispatchTouchEvent(event);
		} else {
			return false;
		}
	}
	
	public void enableTouch(boolean enable) {
		mTouchEnabled = enable;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		final boolean pressed = isPressed();
		if (pressed != mOldPressed) {
			if (!pressed) {
				post(new Runnable() {
					
					@Override
					public void run() {
						callShutterButtonFocus(pressed);
					}
				});
			} else {
				callShutterButtonFocus(pressed);
			}
		}
	}

	private void callShutterButtonFocus(boolean pressed) {
		if (mListener != null) {
			mListener.onShutterButtonFocus(pressed);
		}
	}

	@Override
	public boolean performClick() {
		boolean result = super.performClick();
		if (mListener != null) {
			mListener.onShutterButtonClick();
		}
		
		return result;
	}
	
	
}
