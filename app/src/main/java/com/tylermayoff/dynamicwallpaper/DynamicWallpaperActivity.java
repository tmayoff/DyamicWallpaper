package com.tylermayoff.dynamicwallpaper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DynamicWallpaperActivity extends AppCompatActivity {

    public final int FOLDER_REQUEST = 1;

    Button folderBtn;
    Button sunriseTimeBtn;
    Button sunsetTimeBtn;

    Calendar sunriseTime;
    Calendar sunsetTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_wallpaper);

        sunriseTimeBtn = findViewById(R.id.Sunrise_Button);
        sunsetTimeBtn = findViewById(R.id.Sunset_Button);

        folderBtn = findViewById(R.id.Folder_Button);

        folderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                int code =
                startActivityForResult(intent, FOLDER_REQUEST);
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
        if (resultCode == Activity.RESULT_OK) {


            switch (requestCode) {
                case FOLDER_REQUEST:
                    Uri uri = data.getData();
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
                    String path = getPath(this, docUri);
                    folderBtn.setText(path);
                    

                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}