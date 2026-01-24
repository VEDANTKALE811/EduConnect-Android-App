package com.example.educonnect;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class EduConnectApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sp = getSharedPreferences("MODE", MODE_PRIVATE);
        boolean nightMode = sp.getBoolean("night", false);
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
