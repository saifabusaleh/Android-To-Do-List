package com.todo.saif.todo.activity;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.todo.saif.todo.R;
import com.todo.saif.todo.adapters.ToDoListAdapter;
import com.todo.saif.todo.modal.ToDoData;
import com.todo.saif.todo.sqlite.SqliteHelper;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private FloatingActionButton addTask;
    private static RecyclerView recyclerView;
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ToDoData> tdd = new ArrayList<>();
    private static SqliteHelper mysqlite;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final int RETURN_VALUE_FOR_SAVE = 1;
    public static final int RETURN_VALUE_FOR_CANCEL = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aboutMeMenuItem:
                Intent i = new Intent(this, AboutActivity.class);
                startActivity(i);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_s);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        addTask = (FloatingActionButton) findViewById(R.id.imageButton);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        adapter = new ToDoListAdapter(tdd, getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent), getResources().getColor(R.color.divider));
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                updateCardView();
            }
        });
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                MainActivity.this.startActivityForResult(intent, 1);

            }
        });
    }

    public void isEmptyView(View emptyView) {
        //View emptyView= findViewById(R.id.toDoEmptyView);

        if (adapter != null && emptyView != null) {
            if (adapter.getItemCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                return;
            }
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void updateCardView() {
        swipeRefreshLayout.setRefreshing(true);
        mysqlite = new SqliteHelper(getApplicationContext());
        Cursor result = mysqlite.selectAllData();
        if (result.getCount() == 0) {
            tdd.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "No Tasks", Toast.LENGTH_SHORT).show();
        } else {
            tdd.clear();
            adapter.notifyDataSetChanged();
            while (result.moveToNext()) {
                ToDoData tddObj = new ToDoData();
                tddObj.setToDoID(result.getInt(0));
                tddObj.setToDoTaskDetails(result.getString(1));
                tddObj.setToDoTaskPrority(result.getString(2));
                tddObj.setToDoTaskStatus(result.getString(3));
                tddObj.setToDoNotes(result.getString(4));
                tdd.add(tddObj);
            }
            adapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        isEmptyView(findViewById(R.id.toDoEmptyView));
    }

    @Override
    public void onRefresh() {
        updateCardView();
    }

    private void insertIntoDatabase(ContentValues contentValues) {
        mysqlite = new SqliteHelper(getApplicationContext());
        Boolean b = mysqlite.insertInto(contentValues);
        if (b) {
            updateCardView();
        } else {
            Toast.makeText(getApplicationContext(), "Some thing went wrong with inserting into database", Toast.LENGTH_SHORT).show();
        }
    }

    //Activity result returned from TaskActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RETURN_VALUE_FOR_SAVE)// save
        {
            //Saves toDoItem into database
            ContentValues contentValues = data.getParcelableExtra("ToDoItem");
            insertIntoDatabase(contentValues);
        } else if (resultCode == RETURN_VALUE_FOR_CANCEL)//cancel
        {

        }
    }//onActivityResult

}