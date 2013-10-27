package com.example.testmultiphotos.service.scan.strategy;

import com.example.testmultiphotos.service.scan.QRCodeHandler;

public interface ExtractWorkerStrategy {

  void createWorker(QRCodeHandler handler, int height, int width, byte[] data);

  void stopWorker(QRCodeHandler handler);

  void init();

  String getStrategyName();
}
