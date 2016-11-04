package nutech.hardware.info;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

public class SplashScreen extends Activity {

	private Handler handler = new Handler();
	private long timeDelay = 4000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		VideoView video = (VideoView) findViewById(R.id.splashVideo);
        video.setVideoPath("android.resource://" + getPackageName() + "/"+R.raw.splashvideo);
        video.requestFocus();
        video.start();
		final Intent i = new Intent(SplashScreen.this, MainActivity.class);
		handler.postDelayed(new Runnable() {
			public void run() {
				startActivity(i);
				finish();
			}
		}, timeDelay);
	}
}
