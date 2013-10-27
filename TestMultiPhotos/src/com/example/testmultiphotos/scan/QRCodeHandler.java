package com.example.testmultiphotos.scan;

public interface QRCodeHandler {

  void onNewQRCodeRead(String qrCode);

  void onEndQRCodeRead();
}
