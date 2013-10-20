package com.example.testmultiphotos.scan.strategy.task;

import static com.example.testmultiphotos.Constantes.LOG_TAG;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.example.testmultiphotos.QRCodeHandler;
import com.example.testmultiphotos.scan.strategy.ExtractWorkerStrategy;

public class TaskExtractWorkerStrategy implements ExtractWorkerStrategy {

  public TaskExtractWorkerStrategy() {
    Log.d(LOG_TAG, "Utilisation de " + getStrategyName());
  }

  @Override
  public void createWorker(QRCodeHandler handler, int height, int width, byte[] data) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      new ExtractWorkerAsynTask(handler, height, width).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
    } else {
      new ExtractWorkerAsynTask(handler, height, width).execute(data);
    }
  }

  @Override
  public void stopWorker(QRCodeHandler handler) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
      new WaitWorkerAsynTask(handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    else {
      new WaitWorkerAsynTask(handler).execute(new Void[0]);
    }
  }

  @Override
  public void init() {
  }

  @Override
  public String getStrategyName() {
    return "stratégie par async task";
  }

}
