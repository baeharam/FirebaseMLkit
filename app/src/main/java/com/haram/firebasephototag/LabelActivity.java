package com.haram.firebasephototag;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haram.firebasephototag.database.DBContract;
import com.haram.firebasephototag.database.DBHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class LabelActivity extends AppCompatActivity {

    private ArrayList<String> allLabels;
    private HashMap<String, ArrayList<String>> labelToImage;

    private SQLiteDatabase db;
    private Cursor cursor;
    private Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);

        allLabels = new ArrayList<>();
        labelToImage = new HashMap<>();
        gson = new Gson();

        db = DBHelper.getInstance(this).open();
        String readSQL = "SELECT * FROM " + DBContract.DBEntry.TABLE_NAME;
        cursor = db.rawQuery(readSQL, null);
        while(cursor.moveToNext()) {
            String label = cursor.getString(0);
            String images = cursor.getString(1);
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            allLabels.add(label);
            //noinspection unchecked
            labelToImage.put(label, (ArrayList<String>) gson.fromJson(images, type));
        }

        db.close();

        // Recyclerview 생성 및 Layoutmanager와 adapter 세팅
        RecyclerView recyclerView = findViewById(R.id.label_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(LabelActivity.this));
        recyclerView.setAdapter(new RecyclerviewAdapter());
    }



    private class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view  = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.label_item, viewGroup, false);
            return new textViewHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            textViewHolder holder = (textViewHolder)viewHolder;
            final String label = allLabels.get(i);
            holder.label.setText(label);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LabelActivity.this, PhotoActivity.class);
                    intent.putExtra(getString(R.string.single_label), label);
                    intent.putStringArrayListExtra(label, labelToImage.get(label));
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return allLabels.size();
        }
    }


    private class textViewHolder extends RecyclerView.ViewHolder {
        public TextView label;

        textViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.item_label);
        }
    }
}
