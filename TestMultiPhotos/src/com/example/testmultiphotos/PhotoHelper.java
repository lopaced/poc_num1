package com.example.testmultiphotos;

import static android.widget.Toast.LENGTH_LONG;
import static com.example.testmultiphotos.Constantes.LOG_TAG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class PhotoHelper implements Camera.PreviewCallback, SurfaceHolder.Callback {

	private Context context;
	private Camera camera;
	private boolean isRecording = false;
	private SurfaceHolder holder;

	public PhotoHelper(Context context, SurfaceView surfaceView) {
		this.context = context;
		int numberOfCameras = Camera.getNumberOfCameras();

		if (numberOfCameras < 1) {
			Toast.makeText(context, "Vous n\'avez pas de camera", LENGTH_LONG).show();
		}

		camera = Camera.open();

		Camera.Parameters parameters = camera.getParameters();
		parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
		parameters.setPreviewFormat(ImageFormat.YV12);
		parameters.setPreviewSize(800, 480);
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

		camera.setParameters(parameters);

		holder = surfaceView.getHolder();
		holder.addCallback(this);

		camera.setPreviewCallback(this);
		camera.startPreview();

	}

	public void onPause() {
		if (camera != null) {
			camera.stopPreview();
			isRecording = false;
			Toast.makeText(context, "Fin prise de vue", Toast.LENGTH_SHORT).show();
		}
	}

	public void onStart() {
		if (camera != null) {
			Toast.makeText(context, "Debut prise de vue", Toast.LENGTH_SHORT).show();
			camera.startPreview();
			isRecording = true;
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (isRecording)
			createPicture(data);
	}

	private void createPicture(byte[] data) {

		File picFileDir = new File(Environment.getExternalStorageDirectory(), "testQrScan");

		if (!picFileDir.exists() && !picFileDir.mkdirs()) {
			Log.d(LOG_TAG, "Can't create directory to save image.");
			Toast.makeText(context, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());
		String date = dateFormat.format(new Date());
		String photoFile = "Picture_" + date + ".jpg";

		File pictureFile = new File(picFileDir, photoFile);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
		} catch (Exception error) {
			Log.d(LOG_TAG, "File" + pictureFile.getAbsolutePath() + "not saved: " + error.getMessage());
			Toast.makeText(context, "Image could not be saved.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (camera != null) {
			try {
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (camera != null) {
			try {
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
		}
	}

}
