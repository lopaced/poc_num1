package com.example.testmultiphotos.scan;

import static com.example.testmultiphotos.Constantes.LOG_TAG;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.testmultiphotos.Constantes;
import com.example.testmultiphotos.R;
import com.example.testmultiphotos.scan.strategy.ExtractWorkerStrategy;
import com.example.testmultiphotos.scan.strategy.task.TaskExtractWorkerStrategy;

public class PhotoHelper implements Camera.PreviewCallback, SurfaceHolder.Callback, QRCodeHandler {

  private IScanActivity activity;
  private Camera camera;
  private boolean isRecording = false;
  private SurfaceHolder holder;
  private SurfaceView surfaceView;
  private Set<String> qrCodesFound = new ConcurrentSkipListSet<String>();
  private int cameraId = -1;
  private Size previewSize;
  private long lastFrameProcessing;
  private ExtractWorkerStrategy extractionStrategy;

  private boolean autoFpsRange = true;
  private final int incrementFrameIgnoree = 1000 / Constantes.FRAME_PER_SECONDE;

  public PhotoHelper(IScanActivity activity, SurfaceView surfaceView) {
    this.activity = activity;
    this.surfaceView = surfaceView;
    this.extractionStrategy = new TaskExtractWorkerStrategy();
    // this.extractionStrategy = new ThreadExtractWorkerStrategy();
  }

  public void setExtractStrategy(ExtractWorkerStrategy strategy) {
    extractionStrategy = strategy;
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
    extractionStrategy.stopWorker(this);
  }

  public void onBouttonStart() {
    if (camera == null) {
      activity.showLongToast(R.string.erreur_initialisation_camera);
      return;
    }

    activity.setBoutonStatusToStarting();
    extractionStrategy.init();

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
    extractionStrategy.createWorker(this, previewSize.height, previewSize.width, data);
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
    if (qrCodesFound.add(qrCode)) {
      Log.d(LOG_TAG, "Nouveau QR code " + qrCode);
      activity.playSound(SoundTypeEnum.BIP);
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

}
