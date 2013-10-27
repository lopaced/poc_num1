package com.example.testmultiphotos.client.accueil;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.testmultiphotos.plugins.PluginInfo;

public class PluginsTableRow extends TableRow {

  public PluginsTableRow(Context context) {
    super(context);
  }

  public PluginsTableRow(final Context context, final PluginButtonClickHandler clickHandler,final PluginInfo plugin) {
    this(context);

    ImageButton button = new ImageButton(context);
    button.setImageDrawable(plugin.getIcone());
    button.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        clickHandler.onClick(plugin);
      }
    });

    TextView textView = new TextView(context);
    textView.setText(plugin.getNom());
    addView(button);
    addView(textView);

  }
}
