package com.todo.saif.todo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.todo.saif.todo.sqlite.SqliteHelper;


public class SplashActivity extends AppCompatActivity {
    SqliteHelper mySqliteHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create Sqlite DB
        mySqliteHelper = new SqliteHelper(this);
        // Go to Main Activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
