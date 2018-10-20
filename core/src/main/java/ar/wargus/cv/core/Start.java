package ar.wargus.cv.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import java.util.Arrays;
import java.util.List;

public class Start {

//	public static Start getInstance(CameraManager cameraManager, SurfaceView mContentView){return new Start(cameraManager, mContentView);}

	private final String LOG_TAG = this.getClass().getSimpleName();
	
	private FullscreenActivity fullscreenActivity;
	
	private Surface surfaceTextureView;
	private CameraManager cameraManager;
	private TextureView textureView;
	private Size prefPreviewSize;
	
	private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener(){
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			try {
				setupCamera(width, height);
				openCamera();
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }
		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
	};
	private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(@NonNull CameraDevice camera) {
			try {
				createPreviewSession(camera);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onDisconnected(@NonNull CameraDevice camera) { camera.close(); }
		@Override
		public void onError(@NonNull CameraDevice camera, int error) { camera.close(); }
	};
	private CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
		@Override
		public void onConfigured(@NonNull CameraCaptureSession session) {
			try {
				startPreview(session);
			} catch (Exception e) {
			}
		}
		
		@Override
		public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
	};
	
	public Start() {}

	public void onResume(){
		if(textureView.isAvailable()){
		
		}else{
			textureView.setSurfaceTextureListener(surfaceTextureListener);
		}
	}
	/**
	 *  does stuff
	 * @param fullscreenActivity requires application context to check camera permissions
	 */
	public void doStuff(FullscreenActivity fullscreenActivity) {
		this.fullscreenActivity = fullscreenActivity;
		cameraManager = (CameraManager) fullscreenActivity.getSystemService(Context.CAMERA_SERVICE);
		textureView = fullscreenActivity.findViewById(R.id.fullscreen_content);
		
		if(textureView != null
		   && textureView.getSurfaceTexture() != null)
			surfaceTextureView = new Surface(textureView.getSurfaceTexture());
	}
	
	private void setupCamera(int width, int height) throws CameraAccessException {
		final CameraCharacteristics camChar = cameraManager.getCameraCharacteristics("0");
		prefPreviewSize = getPreviewOutputSize(camChar);
		transform(prefPreviewSize.getWidth(), prefPreviewSize.getHeight());
	}
	
	private void openCamera() throws CameraAccessException {
		if (ActivityCompat.checkSelfPermission(fullscreenActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			Log.e(LOG_TAG, "Illegal state, camera access was invoked without permissions");
			return;
		}
		cameraManager.openCamera("0", cameraStateCallback, null);
	}
	
	private void createPreviewSession(CameraDevice camera) throws CameraAccessException {
		camera.createCaptureSession(Arrays.asList(surfaceTextureView),
		                            sessionStateCallback,
		                            null);
	}
	
	private void startPreview(CameraCaptureSession session) throws CameraAccessException {
		CaptureRequest.Builder builder = session.getDevice()
		                                        .createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
		builder.addTarget(surfaceTextureView);
		session.setRepeatingRequest(builder.build(), null, null);
		
	}
	
	private class SmartSize{
		Size size;
		int longer;
		int shorter;
		SmartSize(int width, int height){
			this.size = new Size(width, height);
			this.longer = Math.max(size.getWidth(),
			                       size.getHeight());
			this.shorter = Math.min(size.getWidth(),
			                        size.getHeight());
		}
	}
	
	private SmartSize getDisplaySmartSize(Context context){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
		
		return new SmartSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
	}
	
	Size getPreviewOutputSize(CameraCharacteristics camChar){
		final double ASPECT_TOLERANCE = 0.5;
		
		SmartSize hdSize = new SmartSize(1080, 720);
		SmartSize displayScreenSize = getDisplaySmartSize(fullscreenActivity);
		boolean hdScreen = displayScreenSize.longer >= hdSize.longer
				           || displayScreenSize.shorter >= hdSize.shorter;
		SmartSize maxSize;
		if(hdScreen) maxSize = displayScreenSize; else maxSize = hdSize;
		
		StreamConfigurationMap config = camChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
		
		List<Size> allSizes = Arrays.asList(config.getOutputSizes(SurfaceTexture.class));
		
		Size size3 = allSizes.stream()
		               .sorted((size1, size2)
                               -> (size2.getHeight()
                                   * size2.getWidth())
		                          - (size1.getHeight()
				                      * size1.getWidth()))
		               .map(size
                            -> new SmartSize(size.getWidth(),
                                             size.getHeight()))
//		               .filter(size
//                               -> size.longer <= maxSize.longer
//		                          && size.shorter <= maxSize.shorter)
		               .filter(size
				               -> {double d = Math.abs(((double) size.longer
						                             /size.shorter)
						                    - ((double) maxSize.longer
								                        /maxSize.shorter));
				               System.out.println("AspectTolerance");
				               System.out.println(d);
				               return d < ASPECT_TOLERANCE;})
		               .findFirst()
                       .orElse(new SmartSize(0,0))
				       .size;
		return size3;
	}
	
	private void transform(int width, int height){
		Matrix matrix = new Matrix();
		int rotation = ((WindowManager) fullscreenActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		RectF textureRectF = new RectF(0, 0, width, height);
		RectF previewRectF = new RectF(0, 0, textureView.getHeight(), textureView.getWidth());
		
		float centerX = textureRectF.centerX();
		float centerY = textureRectF.centerY();
		
		if(rotation == Surface.ROTATION_90
		   || rotation == Surface.ROTATION_270){
			previewRectF.offset(centerX - previewRectF.centerX(),
			                    centerY - previewRectF.centerY());
			matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
			float scale = Math.max((float) width / textureView.getWidth(), (float) height / textureView.getHeight());
			matrix.postScale(scale, scale, centerX, centerY);
			matrix.postRotate(90 * (rotation - 2), centerX, centerY);
		}
		
		textureView.setTransform(matrix);
	}
}
