package com.example.testmultiphotos.scan.strategy.thread;


public class FrameDto {

  private final byte[] datas;
  private ExtractStatusEnum status;

  public FrameDto(byte[] datas) {
    this(datas, ExtractStatusEnum.QUEUED);
  }

  public FrameDto(byte[] datas, ExtractStatusEnum status) {
    this.datas = datas;
    this.status = status;
  }

  public byte[] getDatas() {
    return datas;
  }

  public ExtractStatusEnum getStatus() {
    return status;
  }

  public void setStatus(ExtractStatusEnum status) {
    this.status = status;
  }

}