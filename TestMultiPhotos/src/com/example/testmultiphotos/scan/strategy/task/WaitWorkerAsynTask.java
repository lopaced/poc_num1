package com.example.testmultiphotos.scan.strategy.task;

import static com.example.testmultiphotos.Constantes.LOG_TAG;

import com.example.testmultiphotos.QRCodeHandler;

import android.os.AsyncTask;
import android.util.Log;

public class WaitWorkerAsynTask extends AsyncTask<Void, Void, Void> {

  private QRCodeHandler qrCodeHandler;

  public WaitWorkerAsynTask(QRCodeHandler qrCodeHandler) {
    super();
    this.qrCodeHandler = qrCodeHandler;
  }

  @Override
  protected Void doInBackground(Void... params) {
    while (ExtractWorkerAsynTask.isRunning()) {
      try {
        Log.d(LOG_TAG, "waiting...");
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Log.e(LOG_TAG, e.toString());
      }
    }
    Log.d(LOG_TAG, "Worker finished");
    return null;
  }

  @Override
  protected void onPostExecute(Void result) {
    qrCodeHandler.onEndQRCodeRead();
  }

}
