package com.haram.firebasephototag.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;

public class DataModel implements Parcelable {

    private Uri imageUri;


    public Uri getImageUri() {
        return imageUri;
    }

    public DataModel(String path) {

        this.imageUri = Uri.fromFile(new File(path));
    }

    private DataModel(Parcel in) {
        imageUri = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<DataModel> CREATOR = new Creator<DataModel>() {
        @Override
        public DataModel createFromParcel(Parcel in) {
            return new DataModel(in);
        }

        @Override
        public DataModel[] newArray(int size) {
            return new DataModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(imageUri, i);
    }
}
