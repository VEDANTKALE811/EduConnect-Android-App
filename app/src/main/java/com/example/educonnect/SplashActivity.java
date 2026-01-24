package com.example.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();

        // Initialize Views
        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);
        TextView tagline = findViewById(R.id.tagline);

        // Load Animations
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

        // Apply Animations
        logo.startAnimation(logoAnim);
        appName.startAnimation(fadeIn);
        tagline.startAnimation(fadeIn);

        // Smooth delay and redirect
        new Handler().postDelayed(() -> {
            if (auth.getCurrentUser() != null) {
                String email = auth.getCurrentUser().getEmail();

                // ✅ Redirect based on user role
                if (email != null && email.contains("admin")) {
                    startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, StudentDashboardActivity.class));
                }
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            // Fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();

        }, 2000); // 2 seconds splash delay
    }
}
