package com.tylermayoff.dynamicwallpaper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class DynamicWallpaperActivity extends AppCompatActivity {

    public final int FOLDER_REQUEST = 1;

    // UI Declarations
    Button FolderBtn;

    File themeFolder;
    boolean imagesLoaded;

    ThemeConfig themeConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_wallpaper);

        themeFolder = new File(getFilesDir() + "/theme");
        File[] images = themeFolder.listFiles();
        imagesLoaded = images != null && images.length > 0;
        if (imagesLoaded) {
            themeConfig = new ThemeConfig(themeFolder);
            Intent intent = new Intent(this, DynamicWallpaperService.class);
            intent.putExtra("load", "load");
            startService(intent);
        }

        InitUI();
        InitListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FOLDER_REQUEST) {// Get destination folder
                File dstDir = new File(getFilesDir() + "/theme");
                try {
                    FileUtils.deleteDirectory(dstDir);
                } catch (Exception e) {
                }
                dstDir = new File(getFilesDir() + "/theme");

                Uri uri = data.getData();
                String dirID = DocumentsContract.getTreeDocumentId(uri);
                Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, dirID);
                Cursor c = null;
                try {
                    c = getContentResolver().query(childrenUri, null, null, null, null);
                    while (c.moveToNext()) {

                        String id = c.getString(c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                        String name = c.getString(c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri, id);

                        File newFile = new File(dstDir, name);
                        InputStream is = getContentResolver().openInputStream(docUri);
                        FileUtils.copyInputStreamToFile(is, newFile);
                    }
                    c.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private boolean checkPermissions () {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void InitUI () {
        FolderBtn = findViewById(R.id.Folder_Button);

        TextView nextChangeTxt = findViewById(R.id.NextChange_TextView);
        Calendar nextTime = themeConfig.GetNextTime(themeConfig.GetLastTimeIndex());
        SimpleDateFormat formatter = new SimpleDateFormat("kk:mm");
        nextChangeTxt.setText(formatter.format(nextTime.getTime()));

        if (imagesLoaded)
            FolderBtn.setText("Theme Loaded");
        else
            FolderBtn.setText("Select Folder");

    }

    private void InitListeners() {
        FolderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivityForResult(intent, FOLDER_REQUEST);
                } else {

                }
            }
        });
    }
}