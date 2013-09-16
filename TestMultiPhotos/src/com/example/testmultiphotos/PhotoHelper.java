package com.example.testmultiphotos;

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

  public PhotoHelper(IMainActivity activity, SurfaceView surfaceView) {
    this.activity = activity;
    this.surfaceView = surfaceView;
  }

  public boolean checkPreconditions() {

    if (Camera.getNumberOfCameras() < 1) {
      activity.showLongToast("Vous n\'avez pas de camera");
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
    Camera.Parameters parameters = camera.getParameters();
    parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
    parameters.setPreviewFormat(ImageFormat.YV12);
    Size biggerSupportedSize = parameters.getSupportedPreviewSizes().get(0);
    parameters.setPreviewSize(biggerSupportedSize.width, biggerSupportedSize.height);
    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    camera.setParameters(parameters);

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
    // activity.showShortToast("Fin prise de vue");

    Log.d(LOG_TAG, "Worker stoping");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
      new WaitWorker(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    else {
      new WaitWorker(this).execute(new Void[0]);
    }
  }

  public void onBouttonStart() {
    if (camera != null) {
      camera.autoFocus(new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

          if (!success)
            onBouttonStart();

          activity.showShortToast("Debut prise de vue");
          qrCodesFound.clear();
          isRecording = true;
          startPreview();
        }
      });
    }
  }

  @Override
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
    StringBuffer sb = new StringBuffer();
    sb.append(qrCodesFound.size()).append(" QRcodes trouvés :\n");

    for (String qr : qrCodesFound) {
      sb.append(qr).append("\n");
    }

    activity.showLongToast(sb.toString());
  }
}
