package com.zyh.pddassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button open_btn = findViewById(R.id.turn_on_off);
        serviceIntent = new Intent(this, PddService.class);
        open_btn.setOnClickListener((view -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }));
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }
}