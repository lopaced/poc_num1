package com.example.testmultiphotos;

import static com.example.testmultiphotos.Constantes.HEIGHT;
import static com.example.testmultiphotos.Constantes.WIDTH;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

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

public class ExtractWorker extends AsyncTask<byte[], Void, Set<String>> {

  private QRCodeHandler qrCodeHandler;

  public ExtractWorker(QRCodeHandler qrCodeHandler) {
    super();
    this.qrCodeHandler = qrCodeHandler;
  }

  @Override
  protected Set<String> doInBackground(byte[]... params) {
    if (params != null && params.length > 0)
      return extractQRCodes(params[0]);
    else
      return null;
  }

  /**
   * The system calls this to perform work in the UI thread and delivers the result from doInBackground()
   */
  protected void onPostExecute(Set<String> results) {
    for (String result : results) {
      qrCodeHandler.onNewQRCodeRead(result);
    }
  }

  private Set<String> extractQRCodes(byte[] frame) {
    Log.d(this.getClass().getName(), "extractQRCodes START");
    Set<String> results = new ConcurrentSkipListSet<String>();
    LuminanceSource source = new PlanarYUVLuminanceSource(frame, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    MultipleBarcodeReader reader = new QRCodeMultiReader();

    try {
      Result[] retours = reader.decodeMultiple(bitmap);
      for (Result retour : retours) {
        String qrCode = retour.getText();
        if (results.add(qrCode)) {
          Log.d(this.getClass().getName(), "notification du handler de qr code");
          qrCodeHandler.onNewQRCodeRead(qrCode);
        }
      }
      Log.d(this.getClass().getName(), "extractQRCodes STOP");
    } catch (NotFoundException e) {
      // rien
    } finally {
      return results;
    }
  }

}
