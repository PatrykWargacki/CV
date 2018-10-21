package ar.wargus.cv.core;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
	private View mContentView;
	
	private Start start;
	private void doStuff(){
		if(start == null)
			start = new Start();
		try {
			start.doStuff(this);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(start != null) start.onResume();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Set content view inflates activity view from xml layout file defined in R.layout
		setContentView(R.layout.activity_fullscreen);
		mContentView = findViewById(R.id.fullscreen_content);

		//Init actions after activity view is created
		initAfterCreateView();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull
			                               String[] permissions,
	                                       @NonNull
			                               int[] grantResults) {
		doStuff();
	}
	
	private boolean permissions(String... permissions){
		List<String> requestPermissions = new ArrayList<>();
		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(this,
			                                      permission) != PackageManager.PERMISSION_GRANTED) {
				if (! ActivityCompat.shouldShowRequestPermissionRationale(this,
				                                                          permission)) {
					requestPermissions.add(permission);
				}
			}
		}
		if(!requestPermissions.isEmpty()){
			ActivityCompat.requestPermissions(this,
//			                                  permissions,
			                                  requestPermissions.toArray(new String[requestPermissions.size()]),
			                                  1);
			return false;
		}
		return true;
	}

	//Initialize actions to be performed after activity view is created
	private void initAfterCreateView(){
		//Get activity root layout
		final View activityView = findViewById(android.R.id.content);
		//Add listener to
		activityView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				//Perform actions after layout is created
				if(permissions(Manifest.permission.CAMERA))
					doStuff();
				//Remove listener after view is created
				activityView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
	}
}
