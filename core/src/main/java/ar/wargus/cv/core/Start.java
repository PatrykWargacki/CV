package ar.wargus.cv.core;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;

public class Start {
	
//	public static Start getInstance(CameraManager cameraManager, SurfaceView mContentView){return new Start(cameraManager, mContentView);}
	
	private CameraManager cameraManager;
	private Surface surface;
	
	public Start(CameraManager cameraManager, Surface surface){
		this.cameraManager = cameraManager;
		this.surface = surface;
	}
	
	private CameraDevice.StateCallback cameraStateCallback;
	
	private CameraCaptureSession.StateCallback sessionStateCallback;
	
	private void init(){
		cameraStateCallback = new CameraDevice.StateCallback() {
			@Override
			public void onOpened(@NonNull CameraDevice camera) {
				try {
					camera.createCaptureSession(Arrays.asList(surface), sessionStateCallback, null);
				} catch(Exception e){}
			}
			
			@Override
			public void onDisconnected(@NonNull CameraDevice camera) {}
			
			@Override
			public void onError(@NonNull CameraDevice camera, int error) {}
		};
		sessionStateCallback = new CameraCaptureSession.StateCallback() {
			@Override
			public void onConfigured(@NonNull CameraCaptureSession session) {
				try {
					CaptureRequest.Builder builder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
					builder.addTarget(surface);
//					builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_FAST);
//					builder.set(CaptureRequest.COLOR_CORRECTION_MODE,               CaptureRequest.COLOR_CORRECTION_MODE_FAST);
//					builder.set(CaptureRequest.NOISE_REDUCTION_MODE,                CaptureRequest.NOISE_REDUCTION_MODE_FAST);
//					builder.set(CaptureRequest.CONTROL_AF_MODE,                     CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//					builder.set(CaptureRequest.HOT_PIXEL_MODE,                      CaptureRequest.HOT_PIXEL_MODE_FAST);
//					builder.set(CaptureRequest.SHADING_MODE,                        CaptureRequest.SHADING_MODE_FAST);
//					builder.set(CaptureRequest.TONEMAP_MODE,                        CaptureRequest.TONEMAP_MODE_FAST);
//					builder.set(CaptureRequest.EDGE_MODE,                           CaptureRequest.EDGE_MODE_ZERO_SHUTTER_LAG);
					session.setRepeatingRequest(builder.build(), null, null);
				} catch(Exception e){}
			}
			
			@Override
			public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
		};
	}
	
	public void doStuff() throws Exception{
		init();
		cameraManager.openCamera("0", cameraStateCallback, null);
	}
}
