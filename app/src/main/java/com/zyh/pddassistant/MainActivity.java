package com.zyh.pddassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Button turn_btn;
    private Button open_btn;
    private Button clean_btn;
    private File txtFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        turn_btn = findViewById(R.id.turn_on_off);
        open_btn = findViewById(R.id.open_txt);
        clean_btn = findViewById(R.id.clean_txt);
        txtFile = new File(new ContextWrapper(getApplicationContext()).getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PddService.txt");
        turn_btn.setOnClickListener((view -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }));
        open_btn.setOnClickListener((view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.zyh.pddassistant.fileprovider", txtFile);
            intent.setDataAndType(uri,"text/plain");
            startActivity(intent);
        }));
        clean_btn.setOnClickListener((view -> {
            Utils.cleanFile(txtFile, new Utils.Action() {
                @Override
                public void success() {
                    Toast.makeText(MainActivity.this,"清除成功", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void failure() {
                    Toast.makeText(MainActivity.this,"清除失败", Toast.LENGTH_SHORT).show();
                }
            });
        }));
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }
}