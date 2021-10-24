package dev.mikoto2000.android.sensorrecv;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends AppCompatActivity {

    private OrientationListener ol;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = this.findViewById(R.id.text);

        ol = new OrientationListener();

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (ol != null) {
                    text.append(String.format("方位(磁北が 0): %d\n", ol.getAzimuth()));
                    text.append(String.format("加速度{ x: %f, y: %f, z: %f}\n", ol.getAccelX(), ol.getAccelY(), ol.getAccelZ()));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(r);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ol.resume(getApplicationContext());
    }

    @Override
    protected void onPause() {
        ol.pause();

        super.onPause();
    }
}