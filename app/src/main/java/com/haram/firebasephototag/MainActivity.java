package com.haram.firebasephototag;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.haram.firebasephototag.database.DBContract;
import com.haram.firebasephototag.database.DBHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();

        Button button = findViewById(R.id.main_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(permission) {
                    // 데이터베이스가 이미 생성되어 있을 경우
                    if(checkDB()) {
                        Intent intent = new Intent(MainActivity.this, LabelActivity.class);
                        startActivity(intent);
                    }
                    else {
                        ImagePathTask task = new ImagePathTask(MainActivity.this);
                        task.execute();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "권한이 있어야 합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    boolean checkDB() {
       File dbFile = this.getDatabasePath(DBHelper.DB_NAME);
       return dbFile.exists();
    }

    void permissionCheck() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            permission = true;

        if(!permission) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 200);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==200 && grantResults.length > 0) {
            permission = true;
        }
    }

    private static class ImagePathTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<Activity> activityWeakReference;
        private ArrayList<String> allImages;

        ImagePathTask(Activity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        // 이미지 경로 전부 받아오기
        @Override
        protected Void doInBackground(Void... voids) {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor;
            allImages = new ArrayList<>();
            int column_index_data;
            String projection[] = {MediaStore.MediaColumns.DATA};

            cursor = activityWeakReference.get().getContentResolver().query(uri, projection, null, null, null);
            assert cursor != null;
            column_index_data = cursor.getColumnIndexOrThrow(projection[0]);

            // Cursor 객체 이동하면서 이미지 경로 가져옴
            while(cursor.moveToNext()) {
                allImages.add(cursor.getString(column_index_data));
            }
            cursor.close();
            return null;
        }

        // 작업 완료하면 이미지 경로를 인텐트에 담아서 ProgressActivity 실행
        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(activityWeakReference.get(), ProgressActivity.class);
            intent.putStringArrayListExtra(activityWeakReference.get().getString(R.string.all_images), allImages);
            activityWeakReference.get().startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        DBHelper.getInstance(this).close();
        super.onDestroy();
    }
}
