package com.example.testmultiphotos;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends Activity {

	private PhotoHelper photoHelper;
	private boolean isOn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		photoHelper = new PhotoHelper(getApplicationContext(), (SurfaceView) findViewById(R.id.surfaceView));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		photoHelper.onPause();
		super.onPause();
	}

	public void onClick(View view) {
		if (isOn)
			photoHelper.onPause();
		else
			photoHelper.onStart();

		isOn = !isOn;
	}
}
