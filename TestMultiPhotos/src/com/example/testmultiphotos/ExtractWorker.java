package com.example.testmultiphotos;

import static com.example.testmultiphotos.Constantes.HEIGHT;
import static com.example.testmultiphotos.Constantes.LOG_TAG;
import static com.example.testmultiphotos.Constantes.WIDTH;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import android.os.AsyncTask;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

public class ExtractWorker extends AsyncTask<byte[], Void, Iterable<String>> {

  private QRCodeHandler qrCodeHandler;

  public ExtractWorker(QRCodeHandler qrCodeHandler) {
    super();
    this.qrCodeHandler = qrCodeHandler;
  }

  public static boolean isRunning() {
    return !((ThreadPoolExecutor) THREAD_POOL_EXECUTOR).getQueue().isEmpty();
  }

  @Override
  protected Iterable<String> doInBackground(byte[]... params) {

    if (params != null && params.length > 0) {
      return extractQRCodes(params[0]);
    }

    return new ArrayList<String>(0);
  }

  /**
   * The system calls this to perform work in the UI thread and delivers the result from doInBackground()
   */
  protected void onPostExecute(Iterable<String> results) {
    int nb = 0;
    for (String result : results) {
      ++nb;
      qrCodeHandler.onNewQRCodeRead(result);
      if (nb > 1) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  private Iterable<String> extractQRCodes(byte[] frame) {

    Log.d(LOG_TAG, "extractQRCodes START");

    LuminanceSource source = new PlanarYUVLuminanceSource(frame, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    MultipleBarcodeReader reader = new QRCodeMultiReader();

    List<String> retour = new ArrayList<String>();

    try {
      Result[] results = reader.decodeMultiple(bitmap);
      for (Result result : results) {
        retour.add(result.getText());
      }
    } catch (NotFoundException e) {
      // rien
    }

    Log.d(LOG_TAG, "extractQRCodes END");

    return retour;

  }
}
