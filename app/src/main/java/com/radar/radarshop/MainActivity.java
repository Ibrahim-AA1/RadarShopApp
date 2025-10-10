package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 2000; //2secs

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // splash screen layout:contentReference[oaicite:2]{index=2}

        ImageView imgLogo = findViewById(R.id.imgLogo);     // alpha=0 in XML
        TextView tvName   = findViewById(R.id.tvAppName);   // alpha=0
        View progress     = findViewById(R.id.progress);    // alpha=0

        // Fade in the three elements
        if (imgLogo != null) imgLogo.animate().alpha(1f).setDuration(350).start();
        if (tvName != null)   tvName.animate().alpha(1f).setStartDelay(200).setDuration(350).start();
        if (progress != null) progress.animate().alpha(1f).setStartDelay(400).setDuration(250).start();

        // After a short delay, decide where to go
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            Intent next = session.isLoggedIn()
                    ? new Intent(this, HomeActivity.class)
                    : new Intent(this, AuthActivity.class);
            // clear splash from back stack
            next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(next);
            // no finish() needed because we cleared the task
        }, SPLASH_DURATION_MS);
    }
}
