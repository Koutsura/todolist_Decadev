package com.example.myapplication;

import android.util.Log;
import java.util.Locale;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String title = intent.getStringExtra("taskTitle");
            long reminderTime = intent.getLongExtra("reminderTime", 0);

            // Format waktu menggunakan GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7")); // WIB
            String reminderTimeString = sdf.format(new Date(reminderTime));

            Log.d("ReminderReceiver", "Received alarm for: " + title + " at " + reminderTimeString);

            // Check for POST_NOTIFICATIONS permission
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Notification permission denied. Cannot display reminder.", Toast.LENGTH_SHORT).show();
                Log.w("ReminderReceiver", "POST_NOTIFICATIONS permission not granted");
                return;
            }

            // Create Notification Channel if not exists
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            "Task Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    channel.setDescription("Channel for task reminders");
                    notificationManager.createNotificationChannel(channel);
                    Log.d("ReminderReceiver", "Notification channel created");
                }
            }

            int notificationId = title.hashCode();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText("Reminder at: " + reminderTimeString)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            Log.d("ReminderReceiver", "Notification displayed for: " + title);

        } catch (Exception e) {
            Log.e("ReminderReceiver", "Error processing notification: ", e);
        }
    }
}


