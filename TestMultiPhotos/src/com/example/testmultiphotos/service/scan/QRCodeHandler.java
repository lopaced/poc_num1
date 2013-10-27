package com.example.testmultiphotos.service.scan;

public interface QRCodeHandler {

  void onNewQRCodeRead(String qrCode);

  void onEndQRCodeRead();
}
