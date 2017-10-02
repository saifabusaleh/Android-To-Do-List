package com.todo.saif.todo.activity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.todo.saif.todo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TaskActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private EditText mDateEditText;
    private EditText mTimeEditText;
    private Date mUserReminderDate;
    private String dateFormat = "d MMM, yyyy";
    private String timeFormat = "k:mm";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Button save = (Button) findViewById(R.id.btn_save);
        Button cancel = (Button) findViewById(R.id.btn_cancel);
        CheckBox cb = (CheckBox) findViewById(R.id.checkbox);
        TextView tvstatus = (TextView) findViewById(R.id.status);
        cb.setVisibility(View.GONE);
        tvstatus.setVisibility(View.GONE);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                setResult(MainActivity.RETURN_VALUE_FOR_CANCEL, returnIntent);
                finish();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveTask();
            }
        });

        mDateEditText = (EditText) findViewById(R.id.newTodoDateEditText);
        mTimeEditText = (EditText) findViewById(R.id.newTodoTimeEditText);
        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        mTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        Date currentTime = Calendar.getInstance().getTime();
        mDateEditText.setText(formatDate(dateFormat, currentTime));
        mTimeEditText.setText(formatDate(timeFormat, currentTime));
        Calendar calendar = Calendar.getInstance();
        mUserReminderDate = calendar.getTime();
    }


    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(TaskActivity.this, TaskActivity.this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(TaskActivity.this, TaskActivity.this, hour, minute, true);
        timePickerDialog.show();
    }

    //Function that gets called when trying to save task
    private void onSaveTask() {
        EditText todoText = (EditText) findViewById(R.id.input_task_desc);
        EditText todoNotes = (EditText) findViewById(R.id.input_task_notes);

        if (todoText.getText().length() < 1 || mUserReminderDate == null) {
            Toast.makeText(getApplicationContext(), "To Do Task name and Date and Time must be filled", Toast.LENGTH_SHORT).show();
            //MainActivity.isEmptyView(findViewById(R.id.toDoEmptyView));

            return;
        }
        RadioGroup proritySelection = (RadioGroup) findViewById(R.id.toDoRG);
        String RadioSelection = new String();
        if (proritySelection.getCheckedRadioButtonId() != -1) {
            int id = proritySelection.getCheckedRadioButtonId();
            View radiobutton = proritySelection.findViewById(id);
            int radioId = proritySelection.indexOfChild(radiobutton);
            RadioButton btn = (RadioButton) proritySelection.getChildAt(radioId);
            RadioSelection = (String) btn.getText();
        }
        scheduleNotification(todoText.getText().toString(), RadioSelection);
        ContentValues contentValues = new ContentValues();
        contentValues.put("ToDoTaskDetails", todoText.getText().toString());
        contentValues.put("ToDoTaskPrority", RadioSelection);
        contentValues.put("ToDoTaskStatus", "Incomplete");
        contentValues.put("ToDoNotes", todoNotes.getText().toString());
        Intent returnIntent = new Intent();
        returnIntent.putExtra("ToDoItem", contentValues);
        setResult(MainActivity.RETURN_VALUE_FOR_SAVE, returnIntent);
        finish();
        //MainActivity.insertIntoDatabase(contentValues,dialog);
    }

    // this function schedules notifications depending
    private void scheduleNotification(String TaskTitle, String TaskPrority) {
        Calendar Calendar_Object = Calendar.getInstance();
        Calendar_Object.setTimeInMillis(System.currentTimeMillis());
        Calendar_Object.setTime(mUserReminderDate);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        final int _id = (int) System.currentTimeMillis();
        Intent myIntent = new Intent(TaskActivity.this, AlarmReceiver.class);
        myIntent.putExtra("TaskTitle", TaskTitle);
        myIntent.putExtra("TaskPrority", TaskPrority);
        myIntent.putExtra("id", _id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TaskActivity.this,
                _id, myIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC, Calendar_Object.getTimeInMillis(),
                pendingIntent);
    }

    //function that entered when setting date on time-picker
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int hour, minute;
        Calendar reminderCalendar = Calendar.getInstance();
        reminderCalendar.set(year, month, day);

        if (mUserReminderDate != null) {
            calendar.setTime(mUserReminderDate);
        }
        if (DateFormat.is24HourFormat(this)) {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = calendar.get(Calendar.HOUR);
        }
        minute = calendar.get(Calendar.MINUTE);
        calendar.set(year, month, day, hour, minute);
        mUserReminderDate = calendar.getTime();
        Date d = new Date(year - 1900, month, day);
        mDateEditText.setText(formatDate(dateFormat, d));
    }

    //function that entered when setting date on time-picker
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        if (mUserReminderDate != null) {
            calendar.setTime(mUserReminderDate);
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, hour, minute, 0);
        mUserReminderDate = calendar.getTime();
        mTimeEditText.setText(formatDate(timeFormat, mUserReminderDate));
    }

    private String formatDate(String formatString, Date dateToFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
        return simpleDateFormat.format(dateToFormat);
    }
}
