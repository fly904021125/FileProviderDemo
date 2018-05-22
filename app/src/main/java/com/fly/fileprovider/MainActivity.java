package com.fly.fileprovider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.EMPTY;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                    System.out.println("checkSelfPermission = " + checkSelfPermission);
                    if (checkSelfPermission == PackageManager.PERMISSION_DENIED)
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
                    else createUriFile();
                } else {
                    createUriFile();
                }

            }
        });
    }

    private void createUriFile() {
        Uri uri;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/20180327.mp4");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        uri = FileProvider.getUriForFile(MainActivity.this, "com.fly.fileprovider.fileprovider", file);
        System.out.println("uri = " + uri);
//        System.out.println("uri.getAuthority() = " + uri.getAuthority());
//        System.out.println("uri.getHost() = " + uri.getHost());
//        System.out.println("uri.getScheme() = " + uri.getScheme());
        System.out.println("uri.getPath() = " + uri.getPath());
        ContentResolver contentResolver = getContentResolver();
        Cursor query = contentResolver.query(uri, null, null, null, null);
        if (query != null && query.moveToFirst()) {
//                    int columnIndex = query.getColumnIndex(MediaStore.MediaColumns.DATA);
            for (int i = 0; i < query.getColumnCount(); i++) {

                String string = query.getString(i);
//                System.out.println("string = " + string);
            }
        }
        try {
            final ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                    BufferedInputStream bufferedInput = new BufferedInputStream(inputStream);
                    BufferedOutputStream bufferedOutput = null;
                    try {
                        bufferedOutput = new BufferedOutputStream(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4")));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    byte[] bytes = new byte[1024];
                    int len;
                    try {
                        while ((len = bufferedInput.read(bytes)) != -1) {
                            bufferedOutput.write(bytes, 0, len);

                        }
                        bufferedOutput.flush();
                        bufferedInput.close();
                        bufferedOutput.close();
                        inputStream.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(MainActivity.this, "复制文件成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /**
         * 开启录像
         */
//        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            Uri uri = data.getData();
            System.out.println("uri = " + uri);
            System.out.println(uri.getPath());

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        createUriFile();
                        System.out.println("MainActivity.onRequestPermissionsResult");
                    }
                }
            }
        }
    }
}
