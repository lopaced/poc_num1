package com.example.testmultiphotos.scan.strategy.thread;

import static com.example.testmultiphotos.Constantes.LOG_TAG;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import com.example.testmultiphotos.QRCodeHandler;
import com.example.testmultiphotos.scan.strategy.ExtractWorkerStrategy;

public class ThreadExtractWorkerStrategy implements ExtractWorkerStrategy {

  public ThreadExtractWorkerStrategy() {
    Log.d(LOG_TAG, "Utilisation de " + getStrategyName());
  }

  private BlockingQueue<FrameDto> frames = new ArrayBlockingQueue<FrameDto>(50);
  private WorkerPool workers;

  @Override
  public void createWorker(QRCodeHandler handler, int height, int width, byte[] data) {
    frames.add(new FrameDto(data));

    // Démarrage de l'extraction
    if (workers == null) {
      Log.d(this.getClass().getName(), "WorkerPool starting");
      workers = new WorkerPool(handler, frames, height, width);
      workers.start();
    }

  }

  @Override
  public void stopWorker(QRCodeHandler handler) {
    if (workers != null) {
      workers.stop();
      workers = null;
    }
  }

  @Override
  public void init() {

  }

  @Override
  public String getStrategyName() {
    return "stratégie par thread";
  }

}
