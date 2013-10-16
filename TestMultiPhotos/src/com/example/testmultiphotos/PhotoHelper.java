package com.example.testmultiphotos;

import static com.example.testmultiphotos.Constantes.LOG_TAG;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PhotoHelper implements Camera.PreviewCallback, SurfaceHolder.Callback, QRCodeHandler {

  private IMainActivity activity;
  private Camera camera;
  private boolean isRecording = false;
  private SurfaceHolder holder;
  private SurfaceView surfaceView;
  private Set<String> qrCodesFound = new ConcurrentSkipListSet<String>();
  private int cameraId = -1;
  private Size previewSize;
  private long lastFrameProcessing;
  private BlockingQueue<FrameDto> frames = new ArrayBlockingQueue<FrameDto>(50);
  private WorkerPool workers;

  // False -> Utilisation de l'implémentation ThreadPool
  // True -> Utilisation de l'implémentation AsyncTask
  private static final boolean USING_ASYNC_TASK = false;

  private boolean autoFpsRange = true;
  private final int incrementFrameIgnoree = 1000 / Constantes.FRAME_PER_SECONDE;

  public PhotoHelper(IMainActivity activity, SurfaceView surfaceView) {
    this.activity = activity;
    this.surfaceView = surfaceView;
  }

  public boolean checkPreconditions() {

    if (Camera.getNumberOfCameras() < 1) {
      activity.showLongToast(R.string.erreur_sans_camera);
      return false;
    }

    // Pour s�lectionner l'id de la cam�ra
    CameraInfo cameraInfo = new CameraInfo();
    for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
      Camera.getCameraInfo(i, cameraInfo);
      if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
        cameraId = 0;
      }
    }

    return true;
  }

  public void resume() {
    camera = Camera.open();

    previewSize = findBestPreviewSize();

    Camera.Parameters parameters = camera.getParameters();
    parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
    parameters.setPreviewFormat(ImageFormat.YV12);
    parameters.setPreviewSize(previewSize.width, previewSize.height);
    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    camera.setParameters(parameters);

    try {
      parameters.setPreviewFpsRange(Constantes.FRAME_PER_SECONDE * 1000, Constantes.FRAME_PER_SECONDE * 1000);
      camera.setParameters(parameters);
    } catch (RuntimeException e) {
      // Passsage en mode manuel
      autoFpsRange = false;
    }

    holder = surfaceView.getHolder();
    holder.addCallback(this);

    // Set orientation of camera
    setCameraDisplayOrientation();
    startPreview();

    camera.setPreviewCallback(this);
    camera.startPreview();
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public void onBouttonStop() {
    isRecording = false;

    activity.setBoutonStatusToStopping();

    Log.d(LOG_TAG, "Worker stoping");
    stopWorker();
  }

  public void onBouttonStart() {
    if (camera == null) {
      activity.showLongToast(R.string.erreur_initialisation_camera);
      return;
    }

    activity.setBoutonStatusToStarting();

    camera.autoFocus(new AutoFocusCallback() {
      @Override
      public void onAutoFocus(boolean success, Camera camera) {

        if (!success)
          onBouttonStart();

        activity.showShortToast(R.string.msg_debut_scan);
        activity.setBoutonStatusToStop();
        qrCodesFound.clear();
        isRecording = true;
        startPreview();
      }
    });
  }

  @Override
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public void onPreviewFrame(byte[] data, Camera camera) {
    if (!isRecording) {
      return;
    }

    long currentTimeMillis = System.currentTimeMillis();

    if (!autoFpsRange && lastFrameProcessing + incrementFrameIgnoree > currentTimeMillis) {
      Log.d(LOG_TAG, "Frame non traitée...");
      return;
    }

    lastFrameProcessing = currentTimeMillis;
    createWorker(previewSize.height, previewSize.width, data);
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
    if (camera != null) {
      camera.stopPreview();
      camera.setPreviewCallback(null);
      camera.release();
      camera = null;
    }
  }

  public void startPreview() {
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
  public void onNewQRCodeRead(String qrCode) {
    Log.i(LOG_TAG, "Nouveau QR code " + qrCode);
    if (qrCodesFound.add(qrCode)) {
      activity.playSound(SoundTypeEnum.BIP);
      // activity.showShortToast(qrCode);
    }
  }

  /**
   * Oriente correctement la camera, gestion mode portrait et paysage
   */
  private void setCameraDisplayOrientation() {
    CameraInfo info = new CameraInfo();
    Camera.getCameraInfo(cameraId, info);
    int rotation = ((Activity) activity).getWindowManager().getDefaultDisplay().getRotation();
    int degrees = 0;
    switch (rotation) {
    case Surface.ROTATION_0:
      degrees = 0;
      break;
    case Surface.ROTATION_90:
      degrees = 90;
      break;
    case Surface.ROTATION_180:
      degrees = 180;
      break;
    case Surface.ROTATION_270:
      degrees = 270;
      break;
    }

    int result;
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      result = (info.orientation + degrees) % 360;
      result = (360 - result) % 360; // compensate the mirror
    } else { // back-facing
      result = (info.orientation - degrees + 360) % 360;
    }
    camera.setDisplayOrientation(result);
  }

  @Override
  public void onEndQRCodeRead() {
    StringBuffer sb = new StringBuffer("\n");

    for (String qr : qrCodesFound) {
      sb.append(qr).append("\n");
    }

    int idMessage;

    switch (qrCodesFound.size()) {
    case 0:
      idMessage = R.string.msg_fin_scan_zero;
      break;
    case 1:
      idMessage = R.string.msg_fin_scan_one;
      break;
    default:
      idMessage = R.string.msg_fin_scan_more;
      break;
    }

    activity.showLongToast(idMessage, qrCodesFound.size(), sb.toString());
    activity.setBoutonStatusToStart();
  }

  /** @return La taille optimale de la preview */
  public Size findBestPreviewSize() {
    Size preferredPreviewSize = camera.getParameters().getPreferredPreviewSizeForVideo();

    if (preferredPreviewSize != null)
      return preferredPreviewSize;

    int bestProductSize = 0;

    for (Size curentSize : camera.getParameters().getSupportedPreviewSizes()) {
      int curentProductSize = curentSize.height * curentSize.width;
      if (curentProductSize > bestProductSize) {
        bestProductSize = curentProductSize;
        preferredPreviewSize = curentSize;
      }
    }

    return preferredPreviewSize;
  }

  private void createWorker(int height, int width, byte[] data) {
    if (USING_ASYNC_TASK) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        new ExtractWorkerAsynTask(this, previewSize.height, previewSize.width).executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR, data);
      } else {
        new ExtractWorkerAsynTask(this, previewSize.height, previewSize.width).execute(data);
      }
    } else {
      frames.add(new FrameDto(data));

      // Démarrage de l'extraction
      if (workers == null) {
        Log.d(this.getClass().getName(), "WorkerPool starting");
        workers = new WorkerPool(this, frames, height, width);
        workers.start();
      }
    }
  }

  private void stopWorker() {
    if (USING_ASYNC_TASK) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        new WaitWorker(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
      else {
        new WaitWorker(this).execute(new Void[0]);
      }
    } else {
      if (workers != null) {
        workers.stop();
      }
    }
  }
}
