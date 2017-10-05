package com.todo.saif.todo.activity;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.todo.saif.todo.LoginActivity;
import com.todo.saif.todo.R;
import com.todo.saif.todo.adapters.ToDoListAdapter;
import com.todo.saif.todo.modal.ToDoData;
import com.todo.saif.todo.sqlite.SqliteHelper;
import com.todo.saif.todo.utils.util.ItemTouchHelperClass;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private FloatingActionButton addTask;
    private static RecyclerView recyclerView;
    private static ToDoListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ToDoData> toDoListCopy = new ArrayList<>();
    ArrayList<ToDoData> toDoList = new ArrayList<>();
    private static SqliteHelper mysqlite;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final int RETURN_VALUE_FOR_SAVE = 1;
    public static final int RETURN_VALUE_FOR_CANCEL = 2;
    public ItemTouchHelper itemTouchHelper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        // Associate searchable configuration with the SearchView
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //This function gets called when trying to close the search
                updateCardView();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                //  Toast.makeText(getApplicationContext(), "haha " + newText, Toast.LENGTH_SHORT).show();
                filter(newText);

                return false;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aboutMeMenuItem:
                Intent i = new Intent(this, AboutActivity.class);
                startActivity(i);
                return true;

            case R.id.logoutMenuItem:
                LoginManager.getInstance().logOut();
                goLoginScreen();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        if(AccessToken.getCurrentAccessToken() == null ) {
            goLoginScreen();
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_s);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        addTask = (FloatingActionButton) findViewById(R.id.imageButton);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        adapter = new ToDoListAdapter(toDoList, getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
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
                addTask.setEnabled(false);
                Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                MainActivity.this.startActivityForResult(intent, 1);

            }
        });

        ItemTouchHelper.Callback callback = new ItemTouchHelperClass(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(adapter);

    }

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static void updateView(View emptyView) {
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
        toDoListCopy.clear();
        toDoList.clear();
        if (result.getCount() == 0) {
            adapter.notifyDataSetChanged();
        } else {
            adapter.notifyDataSetChanged();
            while (result.moveToNext()) {
                ToDoData tddObj = new ToDoData();
                tddObj.setToDoID(result.getInt(0));
                tddObj.setToDoTaskDetails(result.getString(1));
                tddObj.setToDoTaskPrority(result.getString(2));
                tddObj.setToDoTaskStatus(result.getString(3));
                tddObj.setToDoNotes(result.getString(4));
                toDoListCopy.add(tddObj);
                toDoList.add(tddObj);
            }
            adapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        updateView(findViewById(R.id.toDoEmptyView));
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
            Toast.makeText(getApplicationContext(), "@string/write_to_db_error", Toast.LENGTH_SHORT).show();
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

            // after saving new task make sure to make the empty view invisible
            updateView(findViewById(R.id.toDoEmptyView));
        }
        addTask.setEnabled(true);

    }//onActivityResult


    // Filter method
    private void filter(String text) {
        text = text.toLowerCase();
        if (text.length() == 0) {
            return;
        }
        toDoList.clear();
        for (ToDoData toDoItem : toDoListCopy) {
            if (toDoItem.getToDoTaskDetails().toLowerCase()
                    .contains(text.toLowerCase())) {
                toDoList.add(toDoItem);
            }
        }

        adapter.notifyDataSetChanged();
    }

}