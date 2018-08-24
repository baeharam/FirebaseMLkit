package com.haram.firebasephototag.database;

import android.provider.BaseColumns;

public final class DBContract {

    private DBContract(){}

    public static class DBEntry implements BaseColumns {
        public static final String TABLE_NAME = "labeling_result";
        public static final String COLUMN_NAME_LABEL = "label";
        public static final String COLUMN_NAME_IMAGES = "images";
    }
}
