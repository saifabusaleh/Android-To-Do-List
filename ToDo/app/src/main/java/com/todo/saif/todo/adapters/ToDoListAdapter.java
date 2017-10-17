package com.todo.saif.todo.adapters;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.todo.saif.todo.R;
import com.todo.saif.todo.activity.AlarmReceiver;
import com.todo.saif.todo.activity.MainActivity;
import com.todo.saif.todo.modal.ToDoData;
import com.todo.saif.todo.sqlite.SqliteHelper;
import com.todo.saif.todo.utils.util.ItemTouchHelperClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ToDoListViewHolder> implements ItemTouchHelperClass.ItemTouchHelperAdapter {
    private List<ToDoData> ToDoDataArrayList = new ArrayList<ToDoData>();
    private Context context;

    // private boolean oneClick=false;//boolean to ensure that we clicked only once in edit or delete
    public ToDoListAdapter(ArrayList<ToDoData> toDoDataArrayList, Context context) {
        this.ToDoDataArrayList = toDoDataArrayList;
        this.context = context;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(ToDoDataArrayList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(ToDoDataArrayList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemRemoved(final int position) {
        final ToDoData td = ToDoDataArrayList.get(position);
        onDeleteTask(td, this.context, position);

    }

    @Override
    public ToDoListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_cardlayout, parent, false);
        ToDoListViewHolder toDoListViewHolder = new ToDoListViewHolder(view, context);
        return toDoListViewHolder;
    }

    @Override
    public void onBindViewHolder(ToDoListViewHolder holder, final int position) {
        final ToDoData td = ToDoDataArrayList.get(position);
        holder.todoDetails.setText(td.getToDoTaskDetails());
        holder.todoNotes.setText(td.getToDoNotes());
        String tdStatus = td.getToDoTaskStatus();
        if (tdStatus.matches("Complete")) {
            holder.todoDetails.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
        String type = td.getToDoTaskPrority();
        int color = 0;
        switch (type) {
            case "Normal":
                color = Color.parseColor("#009EE3");
                break;

            case "Low":
                color = Color.parseColor("#33AA77");
                break;

            case "High":
                color = Color.parseColor("#FF7799");
                break;
        }
        ((GradientDrawable) holder.proprityColor.getBackground()).setColor(color);
    }


    @Override
    public int getItemCount() {
        return ToDoDataArrayList.size();
    }

    private void onDeleteTask(ToDoData td, Context context, final int position) {
        int id = td.getToDoID();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(getApplicationContext(),
                AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 1, myIntent, 0);

        alarmManager.cancel(pendingIntent);
        SqliteHelper mysqlite = new SqliteHelper(context);
        Cursor b = mysqlite.deleteTask(id);
        if (b.getCount() == 0) {
            ToDoDataArrayList.remove(position);
            notifyDataSetChanged();
        } else {
            Toast.makeText(context, "@string/cannot_find_item_in_db", Toast.LENGTH_SHORT).show();
        }
    }

    private void onEditTask(View view, final ToDoData td, final int position) {
        final Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(R.layout.custom_dailog);
        dialog.show();
        EditText todoText = (EditText) dialog.findViewById(R.id.input_task_desc);
        EditText todoNote = (EditText) dialog.findViewById(R.id.input_task_notes);
        CheckBox cb = (CheckBox) dialog.findViewById(R.id.checkbox);
        RadioButton rbHigh = (RadioButton) dialog.findViewById(R.id.high);
        RadioButton rbNormal = (RadioButton) dialog.findViewById(R.id.normal);
        RadioButton rbLow = (RadioButton) dialog.findViewById(R.id.low);
        LinearLayout lv = (LinearLayout) dialog.findViewById(R.id.linearLayout);
        TextView tv = (TextView) dialog.findViewById(R.id.Remainder);
        tv.setVisibility(View.GONE);
        lv.setVisibility(View.GONE);
        if (td.getToDoTaskPrority().matches("Normal")) {
            rbNormal.setChecked(true);
        } else if (td.getToDoTaskPrority().matches("Low")) {
            rbLow.setChecked(true);
        } else {
            rbHigh.setChecked(true);
        }
        if (td.getToDoTaskStatus().matches("Complete")) {
            cb.setChecked(true);
        }
        todoText.setText(td.getToDoTaskDetails());
        todoNote.setText(td.getToDoNotes());
        Button save = (Button) dialog.findViewById(R.id.btn_save);
        Button cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToDoItemOnEdit(dialog, td, view, position);

            }
        });
    }

    private void saveToDoItemOnEdit(Dialog dialog, ToDoData td, View view, int position) {
        EditText todoText = (EditText) dialog.findViewById(R.id.input_task_desc);
        EditText todoNote = (EditText) dialog.findViewById(R.id.input_task_notes);
        CheckBox cb = (CheckBox) dialog.findViewById(R.id.checkbox);
        if (todoText.getText().length() >= 2) {
            RadioGroup proritySelection = (RadioGroup) dialog.findViewById(R.id.toDoRG);
            String RadioSelection = new String();
            if (proritySelection.getCheckedRadioButtonId() != -1) {
                int id = proritySelection.getCheckedRadioButtonId();
                View radiobutton = proritySelection.findViewById(id);
                int radioId = proritySelection.indexOfChild(radiobutton);
                RadioButton btn = (RadioButton) proritySelection.getChildAt(radioId);
                RadioSelection = (String) btn.getText();
            }
            ToDoData updateTd = new ToDoData();
            updateTd.setToDoID(td.getToDoID());
            updateTd.setToDoTaskDetails(todoText.getText().toString());
            updateTd.setToDoTaskPrority(RadioSelection);
            updateTd.setToDoNotes(todoNote.getText().toString());
            if (cb.isChecked()) {
                updateTd.setToDoTaskStatus("Complete");
            }
            updateTd.setToDoTaskStatus("Incomplete");
            SqliteHelper mysqlite = new SqliteHelper(view.getContext());
            Cursor b = mysqlite.updateTask(updateTd);
            ToDoDataArrayList.set(position, updateTd);
            if (b.getCount() == 0) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        // Code here will run in UI thread
                        notifyDataSetChanged();
                    }
                });
            }
            dialog.hide();

        } else {
            Toast.makeText(view.getContext(), "Please enter To Do Task", Toast.LENGTH_SHORT).show();
        }
    }

    public class ToDoListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView todoDetails, todoNotes;
        private ImageButton proprityColor;

        public ToDoListViewHolder(View view, final Context context) {
            super(view);
            view.setOnClickListener(this);
            todoDetails = (TextView) view.findViewById(R.id.toDoTextDetails);
            todoNotes = (TextView) view.findViewById(R.id.toDoTextNotes);
            proprityColor = (ImageButton) view.findViewById(R.id.typeCircle);
        }

        @Override
        public void onClick(View v) {
            final ToDoData td = ToDoDataArrayList.get(getAdapterPosition());
            onEditTask(v, td, getAdapterPosition());
        }
    }
}