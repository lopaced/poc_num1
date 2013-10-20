package com.example.testmultiphotos.scan.strategy.thread;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import com.example.testmultiphotos.QRCodeHandler;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

public class ExtractWorkerThread implements Runnable {

  private QRCodeHandler qrCodeHandler;
  private BlockingQueue<FrameDto> framesQueue;
  private int height, width;

  public ExtractWorkerThread(QRCodeHandler qrCodeHandler, BlockingQueue<FrameDto> framesQueue, int height, int width) {
    super();
    this.framesQueue = framesQueue;
    this.height = height;
    this.width = width;
    this.qrCodeHandler = qrCodeHandler;
  }

  @Override
  public void run() {
    while (true) {
      Log.d(this.getClass().getName(), "Consumer " + Thread.currentThread().getName() + " START");
      try {
        Thread.sleep(new Random().nextInt(100));
        FrameDto frame = framesQueue.take();
        if (frame.getStatus() == ExtractStatusEnum.KILL) {
          Log.d(this.getClass().getName(), "Consumer " + Thread.currentThread().getName() + " KILL");
          break;
        }
        // process queueElement
        extractQRCodes(frame.getDatas());
      } catch (Exception e) {
        e.printStackTrace();
      }
      Log.d(this.getClass().getName(), "Consumer " + Thread.currentThread().getName() + " END");
    }
  }

  private void extractQRCodes(byte[] frame) {
    Log.d(this.getClass().getName(), "extractQRCodes START");
    LuminanceSource source = new PlanarYUVLuminanceSource(frame, width, height, 0, 0, width, height, false);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    MultipleBarcodeReader reader = new QRCodeMultiReader();

    try {
      Result[] retours = reader.decodeMultiple(bitmap);
      for (Result retour : retours) {
        qrCodeHandler.onNewQRCodeRead(retour.getText());
      }
      Log.d(this.getClass().getName(), "extractQRCodes STOP");
    } catch (NotFoundException e) {
      // rien
    } finally {
    }
  }

}