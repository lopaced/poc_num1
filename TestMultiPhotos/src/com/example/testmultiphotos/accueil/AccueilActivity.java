package com.example.testmultiphotos.accueil;

import static com.example.testmultiphotos.Constantes.INTENT_PLUGIN_INFO;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;

import com.example.testmultiphotos.R;
import com.example.testmultiphotos.plugins.PluginInfo;
import com.example.testmultiphotos.plugins.PluginsFactory;
import com.example.testmultiphotos.scan.ScanActivity;

public class AccueilActivity extends Activity implements PluginButtonClickHandler {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_accueil);

    TableLayout pluginTableLayout = (TableLayout) findViewById(R.id.pluginsTable);

    for (PluginInfo info : PluginsFactory.getInstance().getInfos()) {
      pluginTableLayout.addView(new PluginsTableRow(getBaseContext(), this, info));
    }
  }

  @Override
  public void onClick(PluginInfo src) {

    Intent intent = new Intent(getBaseContext(), ScanActivity.class);
    intent.putExtra(INTENT_PLUGIN_INFO, src);

    startActivity(intent);
  }

}
