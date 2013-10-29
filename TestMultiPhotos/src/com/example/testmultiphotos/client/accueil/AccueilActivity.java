package com.example.testmultiphotos.client.accueil;

import static com.example.testmultiphotos.Constantes.INTENT_PLUGIN_INFO;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.testmultiphotos.R;
import com.example.testmultiphotos.client.scan.ScanActivity;
import com.example.testmultiphotos.plugins.PluginInfo;
import com.example.testmultiphotos.plugins.PluginsFactory;

public class AccueilActivity extends Activity implements PluginClickHandler {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_accueil);

    List<Card> cards = new ArrayList<Card>();

    for (PluginInfo info : PluginsFactory.getInstance().getInfos()) {
      cards.add(new PluginsCard(getApplicationContext(), this, info));
    }

    CardGridArrayAdapter mCardArrayAdapter = new CardGridArrayAdapter(getApplicationContext(), cards);

    CardGridView vue = (CardGridView) findViewById(R.id.carddemo);
    vue.setAdapter(mCardArrayAdapter);

  }

  @Override
  public void onClick(PluginInfo plugin) {
    Intent intent = new Intent(getBaseContext(), ScanActivity.class);
    intent.putExtra(INTENT_PLUGIN_INFO, plugin);
    startActivity(intent);
  }

}
