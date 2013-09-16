package com.jingz.app.pano;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.Matrix;
import android.util.Log;

import com.jingz.app.pano.common.ApiHelper;
import com.jingz.app.pano.ui.GLCanvas;
import com.jingz.app.pano.ui.RawTexture;
import com.jingz.app.pano.ui.SurfaceTextureScreenNail;

@TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
public class CameraScreenNail extends SurfaceTextureScreenNail {
	private static final String TAG = "CAM_ScreenNail";
	
	private static final int ANIM_NONE = 0;

	private static final int ANIM_CAPTURE_START = 1;

	private static final int ANIM_CAPTURE_RUNNING = 2;

	private static final int ANIM_SWITCH_COPY_TEXTURE = 3;

	private static final int ANIM_SWITCH_DARK_PREVIEW = 4;

	private static final int ANIM_SWITCH_WAITING_FIRST_FRAME = 5;

	private static final int ANIM_SWITCH_START = 6;

	private static final int ANIM_SWITCH_RUNNING = 7;


	
	private Listener mListener;

	private Object mLock;

	private boolean mFullScreen;

	private int mUncroppedRenderWidth;
	private int mUncroppedRenderHeight;

	private boolean mEnableAspectRatioClamping;

	private int mRenderWidth;

	private int mRenderHeight;

	private float mScaleX;

	private float mScaleY;

	private boolean mFirstFrameArrived;

	private RawTexture mAnimateTexture;

	private int mAnimState;

	private CaptureAnimManager mCaptureAnimManager = new CaptureAnimManager();
	private SwitchAnimManager mSwitchAnimManager = new SwitchAnimManager();
	
	private OnFrameDrawnListener mOneTimeFrameDrawnListener;

	private boolean mVisible;

	private Runnable mOnFrameDrawnListener;

	private float mAlpha;

	private float[] mTextureTransformMatrix;
	
	public interface Listener {
		void requestRender();
		void onPreviewTextureCopied();
		void onCaptureTextureCopied();
	}
	
	public interface OnFrameDrawnListener {
		void onFrameDrawn(CameraScreenNail c);
	}

	
	public CameraScreenNail(Listener listener) {
		mListener = listener;
	}
	
	public void setFullScreen(boolean full) {
		synchronized (mLock) {
			mFullScreen = full;
		}
	}
	
	/**
     * returns the uncropped, but scaled, width of the rendered texture
     */
    public int getUncroppedRenderWidth() {
        return mUncroppedRenderWidth;
    }
    
    /**
     * returns the uncropped, but scaled, width of the rendered texture
     */
    public int getUncroppedRenderHeight() {
        return mUncroppedRenderHeight;
    }
    
    @Override
    public int getWidth() {
        return mEnableAspectRatioClamping ? mRenderWidth : getTextureWidth();
    }

    @Override
    public int getHeight() {
        return mEnableAspectRatioClamping ? mRenderHeight : getTextureHeight();
    }
    
    private int getTextureWidth() {
        return super.getWidth();
    }

    private int getTextureHeight() {
        return super.getHeight();
    }
	
    @Override
	public void setSize(int w, int h) {
		super.setSize(w, h);
		mEnableAspectRatioClamping = false;
		if (mRenderWidth == 0) {
			mRenderWidth = w;
			mRenderHeight = h;
		}
		updateRenderSize();
	}
    
    /**
     * Tells the ScreenNail to override the default aspect ratio scaling
     * and instead perform custom scaling to basically do a centerCrop instead
     * of the default centerInside
     *
     * Note that calls to setSize will disable this
     */
    public void enableAspectRatioClamping() {
        mEnableAspectRatioClamping = true;
        updateRenderSize();
    }
    
    private void setPreviewLayoutSize(int w, int h) {
        Log.i(TAG, "preview layout size: "+w+"/"+h);
        mRenderWidth = w;
        mRenderHeight = h;
        updateRenderSize();
    }

	private void updateRenderSize() {
		if (!mEnableAspectRatioClamping) {
			mScaleX = mScaleY = 1f;
			mUncroppedRenderWidth = getTextureWidth();
			mUncroppedRenderHeight = getTextureHeight();
			return;
		}
		
		float aspectRatio;
		if (getTextureWidth() > getTextureHeight()) {
			aspectRatio = (float) getTextureWidth() / (float) getTextureHeight();
		} else {
			aspectRatio = (float) getTextureHeight() / (float) getTextureWidth();
		}
		
		float scaledTextureWidth;
		float scaledTextureHeight;
		if (mRenderWidth > mRenderHeight) {
			scaledTextureWidth = Math.max(mRenderWidth, 
					(int) (mRenderHeight * aspectRatio));
			scaledTextureHeight = Math.max(mRenderHeight, 
					(int) (mRenderWidth / aspectRatio));
		} else {
			scaledTextureWidth = Math.max(mRenderWidth,
                    (int) (mRenderHeight / aspectRatio));
            scaledTextureHeight = Math.max(mRenderHeight,
                    (int) (mRenderWidth * aspectRatio));
		}
		mScaleX = mRenderWidth / scaledTextureWidth;
		mScaleY = mRenderHeight / scaledTextureHeight;
		mUncroppedRenderWidth = Math.round(scaledTextureWidth);
		mUncroppedRenderHeight = Math.round(scaledTextureHeight);
	}

	
	
	@Override
	public void acquireSurfaceTexture() {
		synchronized (mLock) {
			mFirstFrameArrived = false;
			super.acquireSurfaceTexture();
			mAnimateTexture = new RawTexture(getTextureWidth(), getTextureHeight(), true);
		}
	}

	@Override
	public void releaseSurfaceTexture() {
		synchronized (mLock) {
			super.releaseSurfaceTexture();
			mAnimState = ANIM_NONE; // stop the animation
		}
	}
	
	public void copyTexture() {
		synchronized (mLock) {
			mListener.requestRender();
			mAnimState = ANIM_SWITCH_COPY_TEXTURE;
		}
	}
	
	public void animateSwitchCamera() {
		Log.v(TAG, "animateSwitchCamera");
        synchronized (mLock) {
            if (mAnimState == ANIM_SWITCH_DARK_PREVIEW) {
                // Do not request render here because camera has been just
                // started. We do not want to draw black frames.
                mAnimState = ANIM_SWITCH_WAITING_FIRST_FRAME;
            }
        }
	}
	
	public void animateCapture(int displayRotation) {
        synchronized (mLock) {
            mCaptureAnimManager.setOrientation(displayRotation);
            mCaptureAnimManager.animateFlashAndSlide();
            mListener.requestRender();
            mAnimState = ANIM_CAPTURE_START;
        }
    }
	
	public void animateFlash(int displayRotation) {
        synchronized (mLock) {
            mCaptureAnimManager.setOrientation(displayRotation);
            mCaptureAnimManager.animateFlash();
            mListener.requestRender();
            mAnimState = ANIM_CAPTURE_START;
        }
    }
	
	public void animateSlide() {
        synchronized (mLock) {
            // Ignore the case where animateFlash is skipped but animateSlide is called
            // e.g. Double tap shutter and immediately swipe to gallery, and quickly swipe back
            // to camera. This case only happens in monkey tests, not applicable to normal
            // human beings.
            if (mAnimState != ANIM_CAPTURE_RUNNING) {
                Log.v(TAG, "Cannot animateSlide outside of animateCapture!"
                        + " Animation state = " + mAnimState);
                return;
            }
            mCaptureAnimManager.animateSlide();
            mListener.requestRender();
        }
    }
	
	private void callbackIfNeeded() {
        if (mOneTimeFrameDrawnListener != null) {
            mOneTimeFrameDrawnListener.onFrameDrawn(this);
            mOneTimeFrameDrawnListener = null;
        }
    }
	
	@Override
    protected void updateTransformMatrix(float[] matrix) {
        super.updateTransformMatrix(matrix);
        Matrix.translateM(matrix, 0, .5f, .5f, 0);
        Matrix.scaleM(matrix, 0, mScaleX, mScaleY, 1f);
        Matrix.translateM(matrix, 0, -.5f, -.5f, 0);
    }
	
	public void directDraw(GLCanvas canvas, int x, int y, int width, int height) {
        super.draw(canvas, x, y, width, height);
    }

	@Override
	public void draw(GLCanvas canvas, int x, int y, int width, int height) {
		synchronized (mLock) {
			if (!mVisible) {
				mVisible = true;
			}
			
			SurfaceTexture surfaceTexture = getSurfaceTexture();
			if (surfaceTexture == null || !mFirstFrameArrived) {
				return;
			}
			
			if (mOnFrameDrawnListener != null) {
				mOnFrameDrawnListener.run();
				mOnFrameDrawnListener = null;
			}
			
			float oldAlpha = canvas.getAlpha();
			canvas.setAlpha(mAlpha);
			
			switch (mAnimState) {
			case ANIM_NONE:
				super.draw(canvas, x, y, width, height);
				break;
			case ANIM_SWITCH_COPY_TEXTURE:
				copyPreviewTexture(canvas);
				mSwitchAnimManager.setReviewDrawingSize(width, height);
				mListener.onPreviewTextureCopied();
				mAnimState = ANIM_SWITCH_DARK_PREVIEW;
				break;
			case ANIM_SWITCH_DARK_PREVIEW:
			case ANIM_SWITCH_WAITING_FIRST_FRAME:
				surfaceTexture.updateTexImage();
				mSwitchAnimManager.drawDarkPreview(canvas, x, y, width, 
						height, mAnimateTexture);
				break;
			case ANIM_SWITCH_START:
				mSwitchAnimManager.startAnimation();
				mAnimState = ANIM_SWITCH_RUNNING;
				break;
			case ANIM_CAPTURE_START:
				copyPreviewTexture(canvas);
				mListener.onCaptureTextureCopied();
				mCaptureAnimManager.startAnimation(x, y, width, height);
				mAnimState = ANIM_CAPTURE_RUNNING;
			default:
				break;
			}
			
			if (mAnimState == ANIM_CAPTURE_RUNNING || mAnimState == ANIM_SWITCH_RUNNING) {
				boolean drawn;
				if (mAnimState == ANIM_CAPTURE_RUNNING) {
					if (!mFullScreen) {
						drawn = false;
					} else {
						drawn = mCaptureAnimManager.drawAnimation(canvas, this, mAnimateTexture);
					}
				} else {
					drawn = mSwitchAnimManager.drawAnimation(canvas, x, y, 
							width, height, this, mAnimateTexture);
				}
				
				if (drawn) {
					mListener.requestRender();
				} else {
					mAnimState = ANIM_NONE;
					super.draw(canvas, x, y, width, height);
				}
			}
			canvas.setAlpha(oldAlpha);
			callbackIfNeeded();
		}
	}

	private void copyPreviewTexture(GLCanvas canvas) {
		int width = mAnimateTexture.getWidth();
		int height = mAnimateTexture.getHeight();
		canvas.beginRenderTarget(mAnimateTexture);
		canvas.translate(0, height);
		canvas.scale(1,  -1, 1);
		getSurfaceTexture().getTransformMatrix(mTextureTransformMatrix);
		updateTransformMatrix(mTextureTransformMatrix);
		canvas.drawTexture(mExtTexture, 0, 0, width, height);
		canvas.endRenderTarget();
	}

	@Override
	public void noDraw() {
		synchronized (mLock) {
			mVisible = false;
		}
	}

	@Override
	public void recycle() {
		synchronized (mLock) {
			mVisible = false;
		}
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		synchronized (mLock) {
			if (getSurfaceTexture() != surfaceTexture) {
				return;
			}
			mFirstFrameArrived = true;
			if (mVisible) {
				if (mAnimState == ANIM_SWITCH_WAITING_FIRST_FRAME) {
					mAnimState = ANIM_SWITCH_START;
				}
				
				mListener.requestRender();
			}
		}
	}
	
	// We need to keep track of the size of preview frame on the screen because
    // it's needed when we do switch-camera animation. See comments in
    // SwitchAnimManager.java. This is based on the natural orientation, not the
    // view system orientation.
    public void setPreviewFrameLayoutSize(int width, int height) {
        synchronized (mLock) {
            mSwitchAnimManager.setPreviewFrameLayoutSize(width, height);
            setPreviewLayoutSize(width, height);
        }
    }
    
    public void setOneTimeOnFrameDrawnListener(OnFrameDrawnListener l) {
        synchronized (mLock) {
            mFirstFrameArrived = false;
            mOneTimeFrameDrawnListener = l;
        }
    }
    
    public void setOnFrameDrawnOneShot(Runnable run) {
        synchronized (mLock) {
            mOnFrameDrawnListener = run;
        }
    }

    public float getAlpha() {
        synchronized (mLock) {
            return mAlpha;
        }
    }

    public void setAlpha(float alpha) {
        synchronized (mLock) {
            mAlpha = alpha;
            mListener.requestRender();
        }
    }

}
