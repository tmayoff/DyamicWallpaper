package com.tylermayoff.dynamicwallpaper.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tylermayoff.dynamicwallpaper.R;
import com.tylermayoff.dynamicwallpaper.model.ThemeViewAdapter;
import com.tylermayoff.dynamicwallpaper.model.Theme;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ChangeWallpaper extends Fragment {

    public ChangeWallpaper() { }

    private View root;
    private RecyclerView recyclerView;

    ActivityResultLauncher<Uri> selectFolder = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
        String dirID = DocumentsContract.getTreeDocumentId(uri);
        String[] dirArr =  uri.getLastPathSegment().split("/");
        String dirName = dirArr[dirArr.length - 1];

        // Get destination folder
        File dstDir = new File(getContext().getFilesDir() + "/theme" + dirName);
        try {
            FileUtils.deleteDirectory(dstDir);
        } catch (Exception e) { }
        dstDir = new File(getContext().getFilesDir() + "/theme" + dirName);

        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, dirID);

        Cursor c = null;
        try {
            c = getContext().getContentResolver().query(childrenUri, null, null, null, null);
            while (c.moveToNext()) {

                String id = c.getString(c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                String name = c.getString(c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri, id);

                File newFile = new File(dstDir, name);
                InputStream is = getContext().getContentResolver().openInputStream(docUri);
                FileUtils.copyInputStreamToFile(is, newFile);
            }
            c.close();
        } catch (IOException e) {

        }
    });

    FileFilter ImageTypeFilter = pathname -> {
        try {
            String mimeType = Files.probeContentType(pathname.toPath());
            return mimeType.startsWith("image/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    };

    public static ChangeWallpaper newInstance() {
        ChangeWallpaper fragment = new ChangeWallpaper();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_change_wallpaper, container, false);


        List<Theme> themes = new LinkedList<>();
        File dir = getContext().getFilesDir();
        File[] themeFolders = dir.listFiles();
        for (File theme : themeFolders) {
            File[] images = theme.listFiles(ImageTypeFilter);
            Random r = new Random();
            int rImg = r.nextInt(images.length);
            Bitmap img = BitmapFactory.decodeFile(images[rImg].getAbsolutePath());

            themes.add(new Theme(theme.getName(), img));
        }


        // Adapter Settings
        ThemeViewAdapter adapter = new ThemeViewAdapter(this.getContext(), themes);
        LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        FloatingActionButton AddThemeBtn = root.findViewById(R.id.floatingActionButton);
        AddThemeBtn.setOnClickListener(v -> {
            if (checkPermissions()) {
                selectFolder.launch(null);

            } else {
                // TODO Request permission
            }
        });

        return root;
    }

    private boolean checkPermissions () {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}