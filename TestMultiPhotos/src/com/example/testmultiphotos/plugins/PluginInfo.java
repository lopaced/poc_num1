package com.example.testmultiphotos.plugins;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

public interface PluginInfo extends Serializable {

  String getNom();

  String getDescription();

  /**
   * must be transient !
   * 
   * @return
   */
  Drawable getIcone();
}
