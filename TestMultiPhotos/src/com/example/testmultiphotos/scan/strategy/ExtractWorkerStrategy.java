package com.example.testmultiphotos.scan.strategy;

import com.example.testmultiphotos.QRCodeHandler;

public interface ExtractWorkerStrategy {

  void createWorker(QRCodeHandler handler, int height, int width, byte[] data);

  void stopWorker(QRCodeHandler handler);

  void init();
}