package com.haram.firebasephototag;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class PhotoActivity extends AppCompatActivity {

    private ArrayList<String> allImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        String label = getIntent().getStringExtra(getString(R.string.single_label));
        allImagePath = getIntent().getStringArrayListExtra(label);


        RecyclerView recyclerView = findViewById(R.id.label_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new LabelRecyclerviewAdapter());
    }


    private class LabelRecyclerviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view;
            view = inflater.inflate(R.layout.photo_item, viewGroup, false);
            return new imageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            Glide.with(PhotoActivity.this)
                    .load(Uri.fromFile(new File(allImagePath.get(i))))
                    .into(((imageViewHolder)viewHolder).imageView);
        }

        @Override
        public int getItemCount() {
            return allImagePath.size();
        }
    }

    private class imageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        imageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_imageview);
        }
    }
}
