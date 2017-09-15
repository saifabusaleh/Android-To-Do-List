package com.todo.saif.todo.activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by deepmetha on 8/30/16.
 */
public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        //alaram handler
        if(intent != null) {
            Bundle b = intent.getExtras();
            String TaskTitle = b.getString("TaskTitle");
            String TaskPrority = b.getString("TaskPrority");
            int _id = b.getInt("id");
            Intent notificationServiceIntent = new Intent(context, NotificationService.class);
            notificationServiceIntent.putExtra("TaskTitle", TaskTitle);
            notificationServiceIntent.putExtra("TaskPrority",TaskPrority);
            notificationServiceIntent.putExtra("id",_id);
            //Start notification intent
            context.startService(notificationServiceIntent);
        }

    }
}
