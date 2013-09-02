package com.example.testmultiphotos;

import static com.example.testmultiphotos.Constantes.HEIGHT;
import static com.example.testmultiphotos.Constantes.LOG_TAG;
import static com.example.testmultiphotos.Constantes.WIDTH;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadPoolExecutor;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PhotoHelper implements Camera.PreviewCallback, SurfaceHolder.Callback, QRCodeHandler {

  private Object cameraLock = new Object();
  private IMainActivity activity;
  private Camera camera;
  private boolean isRecording = false;
  private SurfaceHolder holder;
  private SurfaceView surfaceView;
  private Set<String> results = new ConcurrentSkipListSet<String>();

  public PhotoHelper(IMainActivity activity, SurfaceView surfaceView) {
    this.activity = activity;
    this.surfaceView = surfaceView;
  }

  public boolean checkPreconditions() {

    if (Camera.getNumberOfCameras() < 1) {
      activity.showLongToast("Vous n\'avez pas de camera");
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

    startPreview();

    camera.setPreviewCallback(this);
    camera.startPreview();
  }

  public void onBouttonStop() {

    isRecording = false;
    activity.showShortToast("Fin prise de vue");

    Log.d(this.getClass().getName(), "Worker stoping");
    while (!((ThreadPoolExecutor) ExtractWorker.THREAD_POOL_EXECUTOR).getQueue().isEmpty()) {
    }
    Log.d(this.getClass().getName(), "Worker finished");

    StringBuffer sb = new StringBuffer();

    for (String qr : results) {
      sb.append(qr).append("\n");
    }

    activity.showLongToast(sb.toString());
  }

  public void onBouttonStart() {
    synchronized (cameraLock) {
      if (camera != null) {
        camera.autoFocus(new AutoFocusCallback() {
          @Override
          public void onAutoFocus(boolean success, Camera camera) {
            activity.showShortToast("Debut prise de vue");
            isRecording = true;
            startPreview();
          }
        });
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    if (isRecording) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        new ExtractWorker(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
      else {
        new ExtractWorker(this).execute(data);
      }
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    startPreview();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    startPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    stopPreviewAndCamera();
  }

  public void stopPreviewAndCamera() {
    synchronized (cameraLock) {
      if (camera != null) {
        camera.stopPreview();
        camera.release();
        camera = null;
      }
    }
  }

  public void startPreview() {
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

  @Override
  public void onNewQRCodeRead(String qrCode) {
    Log.i(LOG_TAG, "Nouveau QR code " + qrCode);
    activity.showShortToast(qrCode);
    activity.bruitNouveauQRCode();
    results.add(qrCode);
  }
}
