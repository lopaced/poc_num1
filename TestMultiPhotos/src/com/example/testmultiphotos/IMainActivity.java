package com.example.testmultiphotos;

import android.content.res.Resources;

public interface IMainActivity {

  @Deprecated
  void showLongToast(String text);

  void showLongToast(int textId, Object... param);

  @Deprecated
  void showShortToast(String text);

  void showShortToast(int textId, Object... param);

  void playSound(SoundTypeEnum type);

  void setBoutonStatusToStart();

  void setBoutonStatusToStop();

  void setBoutonStatusToStopping();

  void setBoutonStatusToStarting();

  public Resources getResources();

}
