package com.example.testmultiphotos.scan;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.testmultiphotos.R;
import com.example.testmultiphotos.scan.strategy.ExtractWorkerStrategy;
import com.example.testmultiphotos.scan.strategy.task.TaskExtractWorkerStrategy;
import com.example.testmultiphotos.scan.strategy.thread.ThreadExtractWorkerStrategy;

public class ScanActivity extends Activity implements IScanActivity {

  private PhotoHelper photoHelper;
  private boolean isOn = false;
  private boolean isPreconditionsOK;
  private SoundPool soundPool;
  private int startSoundId;
  private int stopSoundId;
  private int bipSoundId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    photoHelper = new PhotoHelper(this, (SurfaceView) findViewById(R.id.surfaceView));
    isPreconditionsOK = photoHelper.checkPreconditions();
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    startSoundId = soundPool.load(this, R.raw.start, 1);
    stopSoundId = soundPool.load(this, R.raw.stop, 1);
    bipSoundId = soundPool.load(this, R.raw.bip, 1);
  }

  @Override
  protected void onResume() {
    super.onResume();

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

    if (isPreconditionsOK) {
      photoHelper.stopPreviewAndCamera();
    }
  }

  public void onClick(View view) {

    if (isOn) {
      // do stop
      playSound(SoundTypeEnum.STOP);
      photoHelper.onBouttonStop();
    } else {
      // do start
      playSound(SoundTypeEnum.START);
      photoHelper.onBouttonStart();
    }

    isOn = !isOn;
  }

  @Override
  public void showLongToast(final String text) {
    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
  }

  @Override
  public void showLongToast(final int textId, Object... param) {
    String msg = getString(textId, param);
    showLongToast(msg);
  }

  @Override
  public void showShortToast(final String text) {
    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void showShortToast(final int textId, Object... param) {
    String msg = getString(textId, param);
    showShortToast(msg);
  }

  @Override
  public void playSound(SoundTypeEnum type) {
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    float volume = actualVolume / maxVolume;
    switch (type) {
    case BIP:
      soundPool.play(bipSoundId, volume, volume, 1, 1, 1f);
      break;
    case START:
      soundPool.play(startSoundId, volume, volume, 1, 1, 1f);
      break;
    case STOP:
      soundPool.play(stopSoundId, volume, volume, 1, 1, 1f);
      break;
    default:
      break;
    }

  }

  @Override
  public void setBoutonStatusToStart() {
    Button btn = (Button) findViewById(R.id.btnStopStart);
    btn.setEnabled(true);
    btn.setText(R.string.btn_start);
  }

  @Override
  public void setBoutonStatusToStarting() {
    Button btn = (Button) findViewById(R.id.btnStopStart);
    btn.setEnabled(false);
    btn.setText(R.string.btn_starting);
  }

  @Override
  public void setBoutonStatusToStop() {
    Button btn = (Button) findViewById(R.id.btnStopStart);
    btn.setEnabled(true);
    btn.setText(R.string.btn_stop);
  }

  @Override
  public void setBoutonStatusToStopping() {
    Button btn = (Button) findViewById(R.id.btnStopStart);
    btn.setEnabled(false);
    btn.setText(R.string.btn_stopping);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    final ExtractWorkerStrategy strategie;

    switch (item.getItemId()) {
    case R.id.selection_strategie_task:
      strategie = new TaskExtractWorkerStrategy();
      break;
    case R.id.selection_strategie_thread:
      strategie = new ThreadExtractWorkerStrategy();
      break;
    default:
      return super.onOptionsItemSelected(item);
    }

    showShortToast(R.string.msg_changement_strategie_scan, strategie.getStrategyName());
    photoHelper.setExtractStrategy(strategie);
    return true;
  }
}