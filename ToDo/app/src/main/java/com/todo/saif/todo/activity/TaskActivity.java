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
import android.widget.Spinner;
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

        mDateEditText = (EditText)findViewById(R.id.newTodoDateEditText);
        mTimeEditText = (EditText)findViewById(R.id.newTodoTimeEditText);
        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date date;

                date = new Date();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog =new DatePickerDialog(TaskActivity.this,TaskActivity.this, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                datePickerDialog.show();

            }
        });

        mTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date date;
//                hideKeyboard(mToDoTextBodyEditText);
//                if(mUserToDoItem.getToDoDate()!=null){
////                    date = mUserToDoItem.getToDoDate();
//                    date = mUserReminderDate;
//                }
//                else{
//                    date = new Date();
//                }
                date = new Date();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(TaskActivity.this, TaskActivity.this, hour, minute, true);
                timePickerDialog.show();
            }
        });
    }

    private void onSaveTask() {
        EditText todoText = (EditText) findViewById(R.id.input_task_desc);
        EditText todoNotes = (EditText) findViewById(R.id.input_task_notes);
      // EditText timeInNumb = (EditText) findViewById(R.id.input_task_time);
        if (todoText.getText().length() >= 2 && mUserReminderDate !=null) {
            RadioGroup proritySelection = (RadioGroup) findViewById(R.id.toDoRG);
            String RadioSelection = new String();
            if (proritySelection.getCheckedRadioButtonId() != -1) {
                int id = proritySelection.getCheckedRadioButtonId();
                View radiobutton = proritySelection.findViewById(id);
                int radioId = proritySelection.indexOfChild(radiobutton);
                RadioButton btn = (RadioButton) proritySelection.getChildAt(radioId);
                RadioSelection = (String) btn.getText();
            }
            Spinner getTime = (Spinner) findViewById(R.id.spinner);
            //parseTime(getTime, 0, todoText, RadioSelection);
            scheduleNotification(0,todoText.getText().toString(),RadioSelection);
            ContentValues contentValues = new ContentValues();
            contentValues.put("ToDoTaskDetails", todoText.getText().toString());
            contentValues.put("ToDoTaskPrority", RadioSelection);
            contentValues.put("ToDoTaskStatus", "Incomplete");
            contentValues.put("ToDoNotes", todoNotes.getText().toString());
            Intent returnIntent = new Intent();
            returnIntent.putExtra("A",contentValues);
            setResult(MainActivity.RETURN_VALUE_FOR_SAVE, returnIntent);
            finish();
            //MainActivity.insertIntoDatabase(contentValues,dialog);
        } else {
            Toast.makeText(getApplicationContext(), "To Do Task and Remind me In fields must be filled", Toast.LENGTH_SHORT).show();
        }

        MainActivity.showEmptyView(findViewById(R.id.toDoEmptyView));
    }

//    private void parseTime(Spinner getTime, EditText timeInNumb,EditText todoText,String RadioSelection) {
//        switch(getTime.getSelectedItem().toString()) {
//            case "Days":
//                int longtime = Integer.parseInt(timeInNumb.getText().toString());
//                long miliTime = longtime * 24 * 60 * 60 * 1000 ;
//                scheduleNotification(miliTime,todoText.getText().toString(),RadioSelection);
//                break;
//            case "Minutes":
//                // Convert timeInNumb of Minutes to Miliseconds
//                longtime = Integer.parseInt(timeInNumb.getText().toString());
//                miliTime = longtime * 60 * 1000 ;
//                scheduleNotification(miliTime,todoText.getText().toString(),RadioSelection);
//                break;
//            case "Hours":
//                // Convert timeInNumb of Hours to Miliseconds
//                longtime = Integer.parseInt(timeInNumb.getText().toString());
//                miliTime = longtime * 60 * 60 * 1000 ;
//                scheduleNotification(miliTime,todoText.getText().toString(),RadioSelection);
//                break;
//        }
//    }
    public void scheduleNotification(long time, String TaskTitle, String TaskPrority) {
        Calendar Calendar_Object = Calendar.getInstance();
        Calendar_Object.setTimeInMillis(System.currentTimeMillis());
        //Calendar_Object.set(mUserReminderDate.getYear()+1900, mUserReminderDate.getMonth(), mUserReminderDate.getDay(), mUserReminderDate.getHours(),
             //   mUserReminderDate.getMinutes(), 0);
        Calendar_Object.setTime(mUserReminderDate);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        final int _id = (int) System.currentTimeMillis();
        Intent myIntent = new Intent(TaskActivity.this, AlarmReceiver.class);
        myIntent.putExtra("TaskTitle", TaskTitle);
        myIntent.putExtra("TaskPrority",TaskPrority);
        myIntent.putExtra("id",_id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TaskActivity.this,
                _id, myIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC, Calendar_Object.getTimeInMillis(),
                pendingIntent);

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int hour, minute;
//        int currentYear = calendar.get(Calendar.YEAR);
//        int currentMonth = calendar.get(Calendar.MONTH);
//        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar reminderCalendar = Calendar.getInstance();
        reminderCalendar.set(year, month, day);

        if(reminderCalendar.before(calendar)){
            Toast.makeText(this, "My time-machine is a bit rusty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mUserReminderDate!=null){
            calendar.setTime(mUserReminderDate);
        }

        if(DateFormat.is24HourFormat(this)){
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        }
        else{

            hour = calendar.get(Calendar.HOUR);
        }
        minute = calendar.get(Calendar.MINUTE);

        calendar.set(year, month, day, hour, minute);
        mUserReminderDate = calendar.getTime();

        //  mUserReminderDate = calendar.getTime();
      //  setReminderTextView();
//        setDateAndTimeEditText();
      //  setDateEditText();

        Date d = new Date(year - 1900,month,day);
        String dateFormat = "d MMM, yyyy";

        mDateEditText.setText(formatDate(dateFormat, d));

    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        if(mUserReminderDate!=null){
            calendar.setTime(mUserReminderDate);
        }

//        if(DateFormat.is24HourFormat(this) && hour == 0){
//            //done for 24h time
//                hour = 24;
//        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, hour, minute, 0);
        mUserReminderDate = calendar.getTime();

     //   setReminderTextView();
//        setDateAndTimeEditText();
       // setTimeEditText();
      //  Date d = new Date(year - 1900,month,day);
        String dateFormat = "k:mm";

        mTimeEditText.setText(formatDate(dateFormat, mUserReminderDate));
    }

    public static String formatDate(String formatString, Date dateToFormat){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
        return simpleDateFormat.format(dateToFormat);
    }

//    public void  setDateEditText(){
//        String dateFormat = "d MMM, yyyy";
//        mDateEditText.setText(formatDate(dateFormat, mUserReminderDate));
//    }
}
