package aris.kots.trackmyday;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class SplashActivity extends AppCompatActivity {

    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");

        TextView app_name = (TextView) findViewById(R.id.title);
        app_name.setTypeface(tf);


//    	Animation fade = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
//    	fade.setDuration(3000);
//    	aris.setImageResource(R.drawable.ariskots2);
//    	aris.startAnimation(fade);


        thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(android.R.anim.fade_in,
                                    android.R.anim.fade_out);
                        }
                    });

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }


    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
        thread.interrupt();
        finish();
    }

}

