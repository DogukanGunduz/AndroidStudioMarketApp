package com.dogukangunduz.marketrnotomasyonuygulamas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dogukangunduz.marketrnotomasyonuygulamas.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        try {
            SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, productName VARCHAR(50), productPrice INTEGER, productStock INTEGER, image BLOB)");
            db.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, userName VARCHAR(50), password VARCHAR(50))");

            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                saveAdmin();
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to create database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void saveAdmin() {
        try {
            SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
            db.execSQL("INSERT INTO users (userName, password) VALUES ('admin', '123456')");
            db.execSQL("INSERT INTO users (userName, password) VALUES ('admin2', '123456')");
            db.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to save admin users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void login(View view) {
        String userName = binding.usernameText.getText().toString();
        String password = binding.passwordText.getText().toString();
        if (userName.equals("") || password.equals("")) {
            Toast.makeText(MainActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        } else {
            try {
                SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
                Cursor cursor = db.rawQuery("SELECT * FROM users WHERE userName = ? AND password = ?", new String[]{userName, password});
                if (cursor.moveToFirst()) {
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, Products.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Invalid User", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Failed to login: ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deleteAllFromDatabase(View view) {
        try {
            SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
            db.execSQL("DELETE FROM products");
            db.close();
            Toast.makeText(MainActivity.this, "Database deleted", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to delete database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
