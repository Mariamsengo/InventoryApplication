package com.example.android.inventoryapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MariamNKinene on 20/07/2017.
 */

public class ProductDbhelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ProductDbhelper.class.getSimpleName();

    // Name of the database file
    private static final String DATABASE_NAME = "products.db";

    //Database version
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ProductDbhelper}.
     *
     * @param context of the app
     */
    public ProductDbhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " ("
                + ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);


    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME);
        onCreate(db);
    }
}