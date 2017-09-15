package com.todo.saif.todo.activity;


import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.todo.saif.todo.R;
import com.todo.saif.todo.adapters.ToDoListAdapter;
import com.todo.saif.todo.modal.ToDoData;
import com.todo.saif.todo.sqlite.SqliteHelper;

import java.util.ArrayList;
import java.util.Calendar;


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
//                final Dialog dialog = new Dialog(MainActivity.this);
//                dialog.setContentView(R.layout.custom_dailog);
//                dialog.show();
//                Button save = (Button) dialog.findViewById(R.id.btn_save);
//                Button cancel = (Button) dialog.findViewById(R.id.btn_cancel);
//                CheckBox cb = (CheckBox) dialog.findViewById(R.id.checkbox);
//                TextView tvstatus = (TextView) dialog.findViewById(R.id.status);
//                cb.setVisibility(View.GONE);
//                tvstatus.setVisibility(View.GONE);
//                cancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dialog.dismiss();
//                    }
//                });
//                save.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        onSaveTask(dialog);
//                    }
//                });
                Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                MainActivity.this.startActivityForResult(intent, 1);

            }
        });
    }

    public static void showEmptyView(View emptyView) {
        //View emptyView= findViewById(R.id.toDoEmptyView);

        if (adapter != null && emptyView != null) {
            if (adapter.getItemCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

    }

    public void scheduleNotification(long time, String TaskTitle, String TaskPrority) {
        Calendar Calendar_Object = Calendar.getInstance();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        final int _id = (int) System.currentTimeMillis();
        Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        myIntent.putExtra("TaskTitle", TaskTitle);
        myIntent.putExtra("TaskPrority", TaskPrority);
        myIntent.putExtra("id", _id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                _id, myIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC, Calendar_Object.getTimeInMillis() + time,
                pendingIntent);

    }

    public void updateCardView() {
        swipeRefreshLayout.setRefreshing(true);
        mysqlite = new SqliteHelper(getApplicationContext());
        Cursor result = mysqlite.selectAllData();
        if (result.getCount() == 0) {
            tdd.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "No Tasks", Toast.LENGTH_SHORT).show();
            //emptyView = (TextView) findViewById(R.id.empty_view);
            // emptyView.setVisibility(View.VISIBLE);
            //recyclerView.setVisibility(View.GONE);

        } else {
            // recyclerView.setVisibility(View.VISIBLE);
            //  emptyView.setVisibility(View.GONE);
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
        showEmptyView(findViewById(R.id.toDoEmptyView));
    }

    @Override
    public void onRefresh() {
        updateCardView();
    }

//    private void onSaveTask(Dialog dialog) {
//        EditText todoText = (EditText) dialog.findViewById(R.id.input_task_desc);
//        EditText todoNotes = (EditText) dialog.findViewById(R.id.input_task_notes);
//        EditText timeInNumb = (EditText) dialog.findViewById(R.id.input_task_time);
//        if (todoText.getText().length() >= 2 && timeInNumb.getText().length() > 0 ) {
//            RadioGroup proritySelection = (RadioGroup) dialog.findViewById(R.id.toDoRG);
//            String RadioSelection = new String();
//            if (proritySelection.getCheckedRadioButtonId() != -1) {
//                int id = proritySelection.getCheckedRadioButtonId();
//                View radiobutton = proritySelection.findViewById(id);
//                int radioId = proritySelection.indexOfChild(radiobutton);
//                RadioButton btn = (RadioButton) proritySelection.getChildAt(radioId);
//                RadioSelection = (String) btn.getText();
//            }
//            Spinner getTime = (Spinner) dialog.findViewById(R.id.spinner);
//            parseTime(getTime, timeInNumb, todoText, RadioSelection);
//            ContentValues contentValues = new ContentValues();
//            contentValues.put("ToDoTaskDetails", todoText.getText().toString());
//            contentValues.put("ToDoTaskPrority", RadioSelection);
//            contentValues.put("ToDoTaskStatus", "Incomplete");
//            contentValues.put("ToDoNotes", todoNotes.getText().toString());
//            //insertIntoDatabase(contentValues,dialog);
//        } else {
//            Toast.makeText(getApplicationContext(), "To Do Task and Remind me In fields must be filled", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void parseTime(Spinner getTime, EditText timeInNumb, EditText todoText, String RadioSelection) {
        switch (getTime.getSelectedItem().toString()) {
            case "Days":
                int longtime = Integer.parseInt(timeInNumb.getText().toString());
                long miliTime = longtime * 24 * 60 * 60 * 1000;
                scheduleNotification(miliTime, todoText.getText().toString(), RadioSelection);
                break;
            case "Minutes":
                // Convert timeInNumb of Minutes to Miliseconds
                longtime = Integer.parseInt(timeInNumb.getText().toString());
                miliTime = longtime * 60 * 1000;
                scheduleNotification(miliTime, todoText.getText().toString(), RadioSelection);
                break;
            case "Hours":
                // Convert timeInNumb of Hours to Miliseconds
                longtime = Integer.parseInt(timeInNumb.getText().toString());
                miliTime = longtime * 60 * 60 * 1000;
                scheduleNotification(miliTime, todoText.getText().toString(), RadioSelection);
                break;
        }
    }

    private void insertIntoDatabase(ContentValues contentValues) {
        mysqlite = new SqliteHelper(getApplicationContext());
        Boolean b = mysqlite.insertInto(contentValues);
        if (b) {
            // dialog.hide();
            updateCardView();
        } else {
            //  Toast.makeText(getApplicationContext(), "Some thing went wrong", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RETURN_VALUE_FOR_SAVE)// save
        {
            ContentValues contentValues = data.getParcelableExtra("A");
            insertIntoDatabase(contentValues);
        } else if (resultCode == RETURN_VALUE_FOR_CANCEL)//cancel
        {

        }
    }//onActivityResult

}