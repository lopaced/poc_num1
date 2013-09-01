package com.example.testmultiphotos;

import static android.widget.Toast.LENGTH_LONG;
import static com.example.testmultiphotos.Constantes.HEIGHT;
import static com.example.testmultiphotos.Constantes.LOG_TAG;
import static com.example.testmultiphotos.Constantes.PICTURE_FILE_NAME_PREFIXE;
import static com.example.testmultiphotos.Constantes.WIDTH;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class PhotoHelper implements Camera.PreviewCallback,
		SurfaceHolder.Callback {

	private Object cameraLock = new Object();
	private Activity activity;
	private Camera camera;
	private boolean isRecording = false;
	private SurfaceHolder holder;
	private File workingDirectory;
	private SurfaceView surfaceView;
	private BlockingQueue<FrameDto> frames = new ArrayBlockingQueue<FrameDto>(
			50);
	private WorkerPool workers;

	public PhotoHelper(Activity context, SurfaceView surfaceView) {
		this.activity = context;
		this.surfaceView = surfaceView;
	}

	public boolean checkPreconditions() {

		int numberOfCameras = Camera.getNumberOfCameras();

		if (numberOfCameras < 1) {
			Toast.makeText(activity, "Vous n\'avez pas de camera", LENGTH_LONG)
					.show();
			return false;
		}

		workingDirectory = new File(Environment.getExternalStorageDirectory(),
				"testQrScan");

		if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
			Log.d(LOG_TAG, "Can't create directory to save image.");
			Toast.makeText(activity, "Can't create directory to save image.",
					Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	public void resume() {
		camera = Camera.open();

		Camera.Parameters parameters = camera.getParameters();
		parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
		parameters.setPreviewFormat(ImageFormat.YV12);
		parameters.setPreviewSize(WIDTH, HEIGHT);
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

		camera.setParameters(parameters);

		holder = surfaceView.getHolder();
		holder.addCallback(this);

		camera.setDisplayOrientation(90);

		onStart();

		camera.setPreviewCallback(this);
		camera.startPreview();
	}

	public void doStop() {
		isRecording = false;
		Toast.makeText(activity, "Fin prise de vue", Toast.LENGTH_SHORT).show();
		if (workers != null) {
			Log.d(this.getClass().getName(),"WorkerPool stoping");
			workers.stop();
			List<String> qrCodes = workers.getResults();
			StringBuffer sb = new StringBuffer();

			for (String qr : qrCodes) {
				sb.append(qr).append("\n");
			}

			Toast.makeText(activity, sb.toString(), Toast.LENGTH_LONG).show();
			workers = null;
		}
	}

	public void doStart() {
		synchronized (cameraLock) {
			if (camera != null) {
				camera.autoFocus(new AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						Toast.makeText(activity, "Debut prise de vue",
								Toast.LENGTH_SHORT).show();
						cleanWorkingDirectory();
						isRecording = true;
						onStart();
					}
				});
			}
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (isRecording) {
			// createPicture(data);
			frames.add(new FrameDto(data));

			// Démarrage de l'extraction
			if (workers == null) {
				Log.d(this.getClass().getName(),"WorkerPool starting");
				workers = new WorkerPool(frames);
				workers.start();
			}

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
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
		synchronized (cameraLock) {
			if (camera != null) {
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		}
	}

	public void onStart() {
		synchronized (cameraLock) {
			if (camera != null) {
				try {
					camera.setPreviewDisplay(holder);
					camera.startPreview();
				} catch (IOException e) {
					Log.e(LOG_TAG, e.getMessage());
				}
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
