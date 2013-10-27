package com.example.testmultiphotos.client.scan;

import android.content.Context;
import android.content.res.Resources;

public interface IScanActivity {

  void playSound(SoundTypeEnum type);

  void setBoutonStatusToStart();

  void setBoutonStatusToStop();

  void setBoutonStatusToStopping();

  void setBoutonStatusToStarting();

  public Resources getResources();

  public Context getApplicationContext();

}
