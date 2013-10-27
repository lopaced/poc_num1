package com.example.testmultiphotos.plugins.mock;

import android.graphics.drawable.Drawable;

import com.example.testmultiphotos.plugins.PluginInfo;

@SuppressWarnings("serial")
public class MockPluginInfo implements PluginInfo {

  private transient Drawable icone;
  private String description;
  private String nom;

  public MockPluginInfo(Drawable icone, String description, String nom) {
    super();
    this.icone = icone;
    this.description = description;
    this.nom = nom;
  }

  @Override
  public String getNom() {
    return nom;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Drawable getIcone() {
    return icone;
  }

}
