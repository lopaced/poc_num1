package com.example.testmultiphotos;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
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
  private SoundPool soundPool;
  private int startSoundId;
  private int stopSoundId;
  private int bipSoundId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    photoHelper = new PhotoHelper(this, (SurfaceView) findViewById(R.id.surfaceView));
    isPreconditionsOK = photoHelper.checkPreconditions();
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    startSoundId = soundPool.load(this, R.raw.start,1);
    stopSoundId = soundPool.load(this, R.raw.stop,1);
    bipSoundId = soundPool.load(this, R.raw.bip,1);
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

    Button btn = (Button) findViewById(R.id.btnStopStart);

    if (isOn) {
      // do stop
      photoHelper.onBouttonStop();
      btn.setText("Start");
      playSound(SoundTypeEnum.STOP);
    } else {
      // do start
      photoHelper.onBouttonStart();
      btn.setText("Stop");
      playSound(SoundTypeEnum.START);
    }

    isOn = !isOn;
  }

  @Override
  public void showLongToast(final String text) {
    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
  }

  @Override
  public void showShortToast(final String text) {
    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void playSound(SoundTypeEnum type) {
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    float actualVolume = (float) audioManager
            .getStreamVolume(AudioManager.STREAM_MUSIC);
    float maxVolume = (float) audioManager
            .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
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
}
