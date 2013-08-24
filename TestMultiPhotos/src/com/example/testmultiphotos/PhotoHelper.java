package com.example.testmultiphotos;

import static android.widget.Toast.LENGTH_LONG;
import static com.example.testmultiphotos.Constantes.LOG_TAG;
import static com.example.testmultiphotos.Constantes.PICTURE_FILE_NAME_PREFIXE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	private File workingDirectory;

	public PhotoHelper(Context context, SurfaceView surfaceView) {
		this.context = context;
		workingDirectory = new File(Environment.getExternalStorageDirectory(), "testQrScan");

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
		// parameters.setRotation(Configuration.ORIENTATION_PORTRAIT);

		camera.setParameters(parameters);

		holder = surfaceView.getHolder();
		holder.addCallback(this);

		camera.setPreviewCallback(this);
		camera.startPreview();

	}

	public void doStop() {
		if (camera != null) {
			isRecording = false;
			onPause();
			Toast.makeText(context, "Fin prise de vue", Toast.LENGTH_SHORT).show();

			try {
				ArrayList<String> qrCodes = QrCodesExtractor.extract(workingDirectory);

				StringBuffer sb = new StringBuffer();

				for (String qr : qrCodes) {
					sb.append(qr).append("\n");
				}

				Toast.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();

			} catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
	}

	public void doStart() {
		if (camera != null) {
			Toast.makeText(context, "Debut prise de vue", Toast.LENGTH_SHORT).show();
			cleanWorkingDirectory();
			isRecording = true;
			onStart();
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (isRecording)
			createPicture(data);
	}

	private void createPicture(byte[] data) {

		if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
			Log.d(LOG_TAG, "Can't create directory to save image.");
			Toast.makeText(context, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());
		String date = dateFormat.format(new Date());

		String photoFile = PICTURE_FILE_NAME_PREFIXE + date + ".jpg";

		File pictureFile = new File(workingDirectory, photoFile);

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
		onStart();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		onStart();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		onPause();
	}

	public void onPause() {
		if (camera != null) {
			camera.stopPreview();
			 camera.release();
		}
	}

	public void onStart() {
		if (camera != null) {
			try {
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
	}

	private void cleanWorkingDirectory() {
		for (File fichier : workingDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith(PICTURE_FILE_NAME_PREFIXE);
			}
		})) {
			fichier.delete();
		}
	}

}
