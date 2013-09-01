package com.example.testmultiphotos;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements IMainActivity {

	private PhotoHelper photoHelper;
	private boolean isOn = false;
	private boolean isPreconditionsOK;
	private MediaPlayer mediaPlayerSacanStart;
	private MediaPlayer mediaPlayerSacanStop;
	private MediaPlayer mediaPlayerQRCodeScaned;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		photoHelper = new PhotoHelper(this, (SurfaceView) findViewById(R.id.surfaceView));
		isPreconditionsOK = photoHelper.checkPreconditions();

	}

	@Override
	protected void onResume() {
		super.onResume();

		mediaPlayerSacanStart = MediaPlayer.create(this, R.raw.start);
		mediaPlayerSacanStop = MediaPlayer.create(this, R.raw.stop);
		mediaPlayerQRCodeScaned = MediaPlayer.create(this, R.raw.bip);

		if (isPreconditionsOK) {
			photoHelper.resume();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();

		mediaPlayerSacanStart.stop();
		mediaPlayerSacanStop.stop();
		mediaPlayerQRCodeScaned.stop();

		if (isPreconditionsOK) {
			photoHelper.stopPreviewAndCamera();
		}
	}

	public void onClick(View view) {

		Button btn = (Button) findViewById(R.id.btnStopStart);

		if (isOn) {
			// do stop
			photoHelper.onBouttonStop();
			btn.setText("Start");
			mediaPlayerSacanStop.start();
		} else {
			// do start
			photoHelper.onBouttonStart();
			btn.setText("Stop");
			mediaPlayerSacanStart.start();
		}

		isOn = !isOn;
	}

	@Override
	public void showLongToast(final String text) {
		// nécessaire car si appel à Toast dans un autre thread que le UI,
		// exception...
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void showShortToast(final String text) {
		// nécessaire car si appel à Toast dans un autre thread que le UI,
		// exception...
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void bruitNouveauQRCode() {
		mediaPlayerQRCodeScaned.start();
	}
}
