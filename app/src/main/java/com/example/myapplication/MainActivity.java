package com.example.myapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import android.content.SharedPreferences;


public class MainActivity extends Activity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private static final int REQUEST_ALARM_PERMISSION = 2;

    private static final String PREFS_NAME = "AppPrefs"; // Nama file SharedPreferences
    private static final String KEY_NOTIFICATION_SHOWN = "NotificationShown"; // Key untuk status notifikasi
    private static final String TAG = "MainActivity";
    RecyclerView recyclerView;
    TaskAdapter adapter;
    ArrayList<Task> tasks;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        tasks = fetchTasks();

        if (tasks != null && !tasks.isEmpty()) {
            adapter = new TaskAdapter(tasks);
            recyclerView.setAdapter(adapter);

            for (Task task : tasks) {
                if (task.getReminderTime() > System.currentTimeMillis()) {
                    scheduleAlarm(task.getReminderTime(), task);
                }
            }
        } else {
            Toast.makeText(this, "No tasks available", Toast.LENGTH_SHORT).show();
        }

        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
        showWelcomeNotification();
        checkAndShowWelcomeNotification();
        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ALARM_PERMISSION) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Exact alarm permission granted.", Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity", "Exact alarm permission granted.");
                } else {
                    Toast.makeText(this, "Exact alarm permission denied.", Toast.LENGTH_LONG).show();
                    Log.d("MainActivity", "Exact alarm permission denied.");
                }
            }, 500);
        }
    }

    private ArrayList<Task> fetchTasks() {
        return databaseHelper.getAllTasks();
    }

    private void scheduleAlarm(long reminderTime, Task task) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (alarmManager != null) {
                Intent intent = new Intent(this, ReminderReceiver.class);
                intent.putExtra("taskTitle", task.getTitle());
                intent.putExtra("reminderTime", reminderTime);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, task.getId(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getDefault());
                String formattedTime = sdf.format(new Date(reminderTime));
                Log.d("MainActivity", "Alarm scheduled successfully for task: " + task.getTitle() + " at " + formattedTime + " (" + TimeZone.getDefault().getID() + ")");
            } else {
                Log.e("MainActivity", "AlarmManager is null. Could not schedule alarm for task: " + task.getTitle());
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error scheduling alarm for task: " + task.getTitle(), e);
            Toast.makeText(this, "Failed to schedule alarm for task: " + task.getTitle(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkAndShowWelcomeNotification() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNotificationShown = sharedPreferences.getBoolean(KEY_NOTIFICATION_SHOWN, false);

        Log.d(TAG, "isNotificationShown: " + isNotificationShown); // Debugging untuk nilai saat ini

        if (!isNotificationShown) {
            showWelcomeNotification();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NOTIFICATION_SHOWN, true);
            boolean success = editor.commit(); // Gunakan commit untuk memastikan penyimpanan berhasil
            Log.d(TAG, "SharedPreferences updated: " + success); // Debugging untuk status penyimpanan
        }
    }

    private void showWelcomeNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) ke atas
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
                return; // Tidak melanjutkan jika izin belum diberikan
            }
        }

        String channelId = "welcome_channel";
        String channelName = "Welcome Notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan ikon aplikasi Anda
                .setContentTitle("Welcome!")
                .setContentText("Welcome to the Task Manager App!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, tampilkan notifikasi selamat datang
                showWelcomeNotification();
            } else {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
