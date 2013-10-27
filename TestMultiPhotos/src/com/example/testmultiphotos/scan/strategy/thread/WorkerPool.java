package com.example.testmultiphotos.scan.strategy.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.testmultiphotos.scan.QRCodeHandler;

import android.util.Log;

public class WorkerPool {

  private static int DEFAULT_SIZE = 1;
  private int size;
  private BlockingQueue<FrameDto> frames;
  private ExecutorService executor;
  private List<Runnable> threads = new ArrayList<Runnable>();
  private int height, width;
  private QRCodeHandler qrCodeHandler;

  public WorkerPool(QRCodeHandler qrCodeHandler, BlockingQueue<FrameDto> frames, int height, int width) {
    this(qrCodeHandler, frames, DEFAULT_SIZE, height, width);
  }

  public WorkerPool(QRCodeHandler qrCodeHandler, BlockingQueue<FrameDto> frames, int size, int height, int width) {
    this.size = size;
    this.frames = frames;
    this.height = height;
    this.width = width;
    this.qrCodeHandler = qrCodeHandler;
  }

  public void start() {
    Log.d(this.getClass().getName(), "WorkerPool START");
    executor = Executors.newFixedThreadPool(size);
    for (int i = 0; i < size; i++) {
      Runnable worker = new ExtractWorkerThread(qrCodeHandler, frames, height, width);
      executor.execute(worker);
      threads.add(worker);
    }
  }

  public void stop() {
    Log.d(this.getClass().getName(), "WorkerPool STOP");
    // Send kill signal to threads
    for (int i = 0; i < size; i++) {
      frames.add(new FrameDto(null, ExtractStatusEnum.KILL));
    }
    while (!frames.isEmpty()) {
    }
    Log.d(this.getClass().getName(), "WorkerPool STOP : Frames empty");
    executor.shutdown();
    while (!executor.isTerminated()) {
    }
    qrCodeHandler.onEndQRCodeRead();
    Log.d(this.getClass().getName(), "WorkerPool STOP : executor terminated");
  }

}