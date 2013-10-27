package com.example.testmultiphotos.plugins;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import com.example.testmultiphotos.plugins.mock.MockPluginInfo;

public class PluginsFactory {

  private static PluginsFactory instance = new PluginsFactory();

  public static PluginsFactory getInstance() {
    return instance;
  }

  private PluginsFactory() {
  }

  public Iterable<PluginInfo> getInfos() {

    List<PluginInfo> retour = new ArrayList<PluginInfo>();
    Drawable a = PictureDrawable.createFromStream(getClass().getResourceAsStream("/res/drawable-hdpi/ic_launcher.png"),
        "");

    for (int i = 0; i < 5; i++)
      retour.add(new MockPluginInfo(a, "Description" + i, "plugin num " + i));

    return retour;
  }
}
