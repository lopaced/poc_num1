package com.example.testmultiphotos.client.accueil;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testmultiphotos.plugins.PluginInfo;

public class PluginsCard extends Card implements Card.OnCardClickListener {

  private PluginInfo plugin;
  private PluginClickHandler clickHandler;

  public PluginsCard(final Context context, final PluginClickHandler clickHandler, final PluginInfo plugin) {
    super(context);
    this.plugin = plugin;
    this.clickHandler = clickHandler;

    setOnClickListener(this);
  }

  @Override
  public void setupInnerViewElements(ViewGroup parent, View view) {

    TextView textView = new TextView(getContext());
    textView.setText(plugin.getNom());

    ImageView imageView = new ImageView(getContext());
    imageView.setBackground(plugin.getIcone());

    parent.addView(imageView);
    parent.addView(textView);
  }

  @Override
  public void onClick(Card arg0, View arg1) {
    clickHandler.onClick(plugin);
  }
}
