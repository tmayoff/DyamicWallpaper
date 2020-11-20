package com.tylermayoff.dynamicwallpaper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TimePicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

public class DynamicWallpaperActivity extends AppCompatActivity {

    public final int FOLDER_REQUEST = 1;
    public final int PERMISSION_REQUEST = 2;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor prefEditor;

    ImageView previewImage;

    Button folderBtn;
    Button sunriseTimeBtn;
    Button sunsetTimeBtn;

    String themePath;

    Calendar sunriseTime;
    Calendar sunsetTime;

    LinkedList<Bitmap> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_wallpaper);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefEditor = sharedPreferences.edit();

        themePath = sharedPreferences.getString("themePath", "");

        previewImage = findViewById(R.id.Preview_ImageView);

        sunriseTimeBtn = findViewById(R.id.Sunrise_Button);
        sunsetTimeBtn = findViewById(R.id.Sunset_Button);

        folderBtn = findViewById(R.id.Folder_Button);
        if (themePath != "")
            folderBtn.setText(themePath);

        getImagePreview();

        folderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, FOLDER_REQUEST);
                } else {
                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                }
            }
        });

        sunriseTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();

                TimePickerDialog t = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        sunriseTime = new GregorianCalendar(0, 0, 0, hourOfDay, minute, 0);
                        sunriseTimeBtn.setText(sunriseTime.get(Calendar.HOUR_OF_DAY) + ":" + sunriseTime.get(Calendar.MINUTE));
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                t.show();
            }
        });

        sunsetTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();

                TimePickerDialog t = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        sunsetTime = new GregorianCalendar(0, 0, 0, hourOfDay, minute, 0);
                        sunsetTimeBtn.setText(sunsetTime.get(Calendar.HOUR_OF_DAY) + ":" + sunsetTime.get(Calendar.MINUTE));
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                t.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case FOLDER_REQUEST:
                    Uri uri = data.getData();
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    themePath = ASFUriHelp.getPath(this, docUri);
                    prefEditor.putString("themePath", themePath);
                    prefEditor.apply();
                    folderBtn.setText(themePath);

                    getImagePreview();
                    break;
                case PERMISSION_REQUEST:
                    Log.d("Intent", data.getDataString());
                    break;
            }
        }
    }

    void getImagePreview() {
        if (themePath == "")
            return;

        File dir = new File(themePath);
        File[] files = dir.listFiles();
        if (files.length == 0)
            return;

        images = new LinkedList<>();
        for (File f: files) {
            Bitmap img = BitmapFactory.decodeFile(f.getAbsolutePath());
            if (img != null)
                images.add(img);
        }

        previewImage.setImageBitmap(images.get(0));
    }
}