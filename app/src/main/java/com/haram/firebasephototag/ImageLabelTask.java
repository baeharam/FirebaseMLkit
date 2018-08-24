package com.haram.firebasephototag;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.gson.Gson;
import com.haram.firebasephototag.database.DBContract;
import com.haram.firebasephototag.database.DBHelper;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ImageLabelTask extends AsyncTask<Void, Double, Void> {

    // 메모리를 누수시킬 수 있는 객체들은 WeakReference로 참조한다.
    private WeakReference<Activity> activity;
    private WeakReference<ProgressBar> progressBar;
    private WeakReference<TextView> progress;

    // 인텐트
    private Intent intent;

    // 이미지 라벨링
    private int totalImageNumber, numOfImage;
    private FirebaseVisionLabelDetector detector;
    private HashMap<String, Integer> allLabels;
    private ArrayList<String> allImageUris;
    private HashMap<String, ArrayList<String>> labelToImage;

    // 데이터베이스
    private SQLiteDatabase db;

    ImageLabelTask(Activity activity, ProgressBar progressBar, TextView progress) {
        this.activity = new WeakReference<>(activity);
        this.progressBar = new WeakReference<>(progressBar);
        this.progress = new WeakReference<>(progress);

        intent = this.activity.get().getIntent();

        detector = FirebaseVision.getInstance().getVisionLabelDetector();
        allLabels = new HashMap<>();
        labelToImage = new HashMap<>();


        db = DBHelper.getInstance(activity).open();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        // 인텐트로 모든 이미지의 경로를 받아온다.
        final List<String> allImages = intent.getStringArrayListExtra(activity.get().getString(R.string.all_images));
        totalImageNumber = allImages.size();

        // 모든 이미지 경로에 대해 반복
        for(final String path : allImages) {
            // 이미지 경로를 이용해 비트맵으로 변환
            final Bitmap bitmap = BitmapFactory.decodeFile(path);
            // 비트맵으로 변환되지 않는 이미지 예외처리
            if(bitmap!=null) {
                // FirebaseVisionImage 객체 생성해서 detector를 이용해 라벨링 시작
                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
                detector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                        // 라벨링 성공할 경우
                        for(FirebaseVisionLabel label : firebaseVisionLabels) {
                            // 해당 라벨이 allLabels에 몇개 있는지 알아냄
                            Integer labelNum = allLabels.get(label.getLabel());

                            // 1개도 없을 경우 0으로 초기화
                            if(labelNum==null) labelNum=0;

                            // 라벨, 해당 이미지 Uri 받아옴(DataModel로)
                            allLabels.put(label.getLabel(), labelNum+1);
                            allImageUris = labelToImage.get(label.getLabel());

                            // Uri가 하나도 없다면 생성
                            if(allImageUris ==null) {
                                allImageUris = new ArrayList<>();
                            }
                            // 이미지 경로를 Uri로 변환시켜 저장
                            allImageUris.add(path);
                            // 다시 labelToImage에 라벨과 연관시켜 저장
                            labelToImage.put(label.getLabel(), allImageUris);
                        }

                        // 이미지 하나 라벨링 했으니 개수 증가
                        numOfImage++;
                        // 얼마나 진행했는지 계산
                        Double percent = ((double)numOfImage/(double) totalImageNumber)*100.0;
                        DecimalFormat df = new DecimalFormat("#.##");
                        publishProgress(Double.parseDouble(df.format(percent)));
                    }
                });
                // 비트맵으로 변환 안될 경우 이미지 개수 감소시킴 (퍼센트 위해서)
            } else {
                totalImageNumber--;
            }
        }


        // 라벨들과 해당 이미지 경로들 sqlite에 저장
        for(Map.Entry<String, Integer> entry : allLabels.entrySet()) {
            Gson gson = new Gson();
            String images = gson.toJson(labelToImage.get(entry.getKey()));

            String insertSQL = "INSERT INTO '" + DBContract.DBEntry.TABLE_NAME + "' VALUES ('"+entry.getKey()+"', '"+images+"')";
            db.execSQL(insertSQL);
        }
        return null;
    }

    // 진행 상황 표시
    @Override
    protected void onProgressUpdate(Double... values) {
        progressBar.get().setProgress(values[0].intValue());
        String percent = String.valueOf(values[0])+"%";
        progress.get().setText(percent);
    }

    // 라벨, 아이디, 신뢰도, 라벨에 해당하는 이미지 전부 LabelActivity로 넘김
    @Override
    protected void onPostExecute(Void aVoid) {
        Activity progressActivity = activity.get();
        Intent intent = new Intent(progressActivity, LabelActivity.class);
        progressActivity.startActivity(intent);
        progressActivity.finish();
    }
}
