package com.alexandru.saia.augmentedreality;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by krzysztofjackowski on 24/09/15.
 * and alexandru
 */
public class CameraViewActivity extends Activity implements
		SurfaceHolder.Callback, OnLocationChangedListener, OnAzimuthChangedListener{

    private static final int MY_LOCATION_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean isCameraviewOn = false;
	private AugmentedPOI mPoi;

	private double mAzimuthReal = 0;
	private double mAzimuthTeoretical = 0;
	private static double AZIMUTH_ACCURACY = 5;
	private double mMyLatitude = 0;
	private double mMyLongitude = 0;

	private MyCurrentAzimuth myCurrentAzimuth;
	private MyCurrentLocation myCurrentLocation;

	TextView descriptionTextView;
	ImageView pointerIcon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setupListeners();
		setupLayout();

	}

	private void setAugmentedRealityPoint() {
		mPoi = new AugmentedPOI(
				"Alex Dascalu",
				"Alex Dascalu",
				50.06169631,
				19.93919566
		);
	}

	public double calculateTeoreticalAzimuth() {
		double dX = mPoi.getPoiLatitude() - mMyLatitude;
		double dY = mPoi.getPoiLongitude() - mMyLongitude;

		double phiAngle;
		double tanPhi;

        tanPhi = Math.abs(dY / dX);
		phiAngle = Math.atan(tanPhi);
		phiAngle = Math.toDegrees(phiAngle);

		if (dX > 0 && dY > 0) { // I quater
			return phiAngle;
		} else if (dX < 0 && dY > 0) { // II
			return 180 - phiAngle;
		} else if (dX < 0 && dY < 0) { // III
			return 180 + phiAngle;
		} else if (dX > 0 && dY < 0) { // IV
			return 360 - phiAngle;
		}

		return phiAngle;
	}
	
	private List<Double> calculateAzimuthAccuracy(double azimuth) {
		double minAngle = azimuth - AZIMUTH_ACCURACY;
		double maxAngle = azimuth + AZIMUTH_ACCURACY;
		List<Double> minMax = new ArrayList<>();

		if (minAngle < 0)
			minAngle += 360;

		if (maxAngle >= 360)
			maxAngle -= 360;

		minMax.clear();
		minMax.add(minAngle);
		minMax.add(maxAngle);

		return minMax;
	}

	private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
		if (minAngle > maxAngle) {
            return isBetween(0, maxAngle, azimuth) && isBetween(minAngle,
                    360, azimuth);
		} else {
            return azimuth > minAngle && azimuth < maxAngle;
		}
    }

	private void updateDescription() {
        String sb = mPoi.getPoiName() +
                " azimuthTheoretical " +
                mAzimuthTeoretical +
                " azimuthReal " +
                mAzimuthReal +
                " latitude " +
                mMyLatitude +
                " longitude " +
                mMyLongitude;
        descriptionTextView.setText(sb);
	}

	@Override
	public void onLocationChanged(Location location) {
		mMyLatitude = location.getLatitude();
		mMyLongitude = location.getLongitude();
		mAzimuthTeoretical = calculateTeoreticalAzimuth();
		Toast.makeText(this,"latitude: "+location.getLatitude()+" longitude: " +
                location.getLongitude(), Toast.LENGTH_SHORT).show();
		updateDescription();
	}

	@Override
	public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {
		mAzimuthReal = azimuthChangedTo;
		mAzimuthTeoretical = calculateTeoreticalAzimuth();

		pointerIcon = findViewById(R.id.icon);

		double minAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
		double maxAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);

//		if (isBetween(minAngle, maxAngle, mAzimuthReal)) {
//			pointerIcon.setVisibility(View.VISIBLE);
//		} else {
//			pointerIcon.setVisibility(View.INVISIBLE);
//		}

        ImageView img_north = findViewById(R.id.icon_north);
        img_north.setVisibility(View.INVISIBLE);
        ImageView img_south = findViewById(R.id.icon_south);
        img_south.setVisibility(View.INVISIBLE);
        ImageView img_west = findViewById(R.id.icon_west);
        img_west.setVisibility(View.INVISIBLE);
        ImageView img_east = findViewById(R.id.icon_east);
        img_east.setVisibility(View.INVISIBLE);

		if (is_around(360 - 10, mAzimuthReal) ||
                is_around(0, mAzimuthReal))
		{
			//pointerIcon.setImageResource();
			img_north.setVisibility(View.VISIBLE);
		}
		else if (is_around(270 - 10, mAzimuthReal))
        {
            img_west.setVisibility(View.VISIBLE);
        }
        else if (is_around(180 - 10, mAzimuthReal))
        {
            img_south.setVisibility(View.VISIBLE);
        }
        else if (is_around(90 - 10, mAzimuthReal))
        {
            img_east.setVisibility(View.VISIBLE);
        }

		updateDescription();
	}

	private boolean is_around(double angle_point, double mAzimuthTeoretical) {
		int DELTA = 5;
		return Math.abs(angle_point - mAzimuthTeoretical) < DELTA;
	}

	@Override
	protected void onStop() {
		myCurrentAzimuth.stop();
		myCurrentLocation.stop();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted();
		} else {
			// Show rationale and request permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode)
        {
            case MY_LOCATION_REQUEST_CODE:
                if (permissions.length == 1 &&
                        permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted();
                } else {
                    // Permission was denied. Display an error message.
                    Toast.makeText(this,
                            "App needs location permission", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
            case CAMERA_REQUEST_CODE:
                if (permissions.length == 1 &&
                        permissions[0].equals(Manifest.permission.CAMERA) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraPermissionGranted();
                } else {
                    // Permission was denied. Display an error message.
                    Toast.makeText(this,
                            "App needs camera permission", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
        }
    }

    private void locationPermissionGranted() {
        myCurrentLocation.buildGoogleApiClient(this);

	    myCurrentAzimuth.start();
        myCurrentLocation.start();
    }

	private void setupListeners() {
		myCurrentLocation = new MyCurrentLocation(this);
		myCurrentAzimuth = new MyCurrentAzimuth(this, this);
	}

	private void setupLayout() {
		descriptionTextView = (TextView) findViewById(R.id.cameraTextView);

		getWindow().setFormat(PixelFormat.UNKNOWN);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
		mSurfaceHolder = surfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		if (isCameraviewOn) {
			mCamera.stopPreview();
			isCameraviewOn = false;
		}

		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.startPreview();
				isCameraviewOn = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraPermissionGranted();
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }

	}

    private void cameraPermissionGranted() {
        mCamera = Camera.open();

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int rotation = this.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(result);
        setAugmentedRealityPoint();
    }

    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
		isCameraviewOn = false;
	}
}
