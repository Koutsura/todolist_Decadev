package com.example.myapplication;


import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;


public class AddTaskActivity extends AppCompatActivity {
    private EditText titleInput, descriptionInput;
    private Button setReminderButton, saveButton;
    private DatabaseHelper databaseHelper;
    private long reminderTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);


        titleInput = findViewById(R.id.taskTitleInput);
        descriptionInput = findViewById(R.id.taskDescriptionInput);
        setReminderButton = findViewById(R.id.setReminderTime);
        saveButton = findViewById(R.id.saveButton);
        databaseHelper = new DatabaseHelper(this);

        // Set reminder time with TimePickerDialog
        setReminderButton.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            new TimePickerDialog(this, (view1, selectedHour, selectedMinute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                reminderTime = calendar.getTimeInMillis();


                setReminderButton.setText(String.format(Locale.getDefault(), "Reminder set for: %02d:%02d", selectedHour, selectedMinute));
            }, hour, minute, true).show();
        });


        saveButton.setOnClickListener(view -> {
            String title = titleInput.getText().toString();
            String description = descriptionInput.getText().toString();

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in both title and description", Toast.LENGTH_SHORT).show();
                return;
            }


            Task newTask = new Task(title, description, reminderTime);
            boolean success = databaseHelper.addTask(newTask);

            if (success) {
                if (reminderTime != 0) {
                    Intent mainIntent = new Intent(AddTaskActivity.this, MainActivity.class);
                    mainIntent.putExtra("taskId", newTask.getId());
                    mainIntent.putExtra("reminderTime", reminderTime);
                    mainIntent.putExtra("taskTitle", title);
                    startActivity(mainIntent);
                    Toast.makeText(this, "Task saved with reminder", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Task saved without reminder", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
            }

            finish();
        });
    }
}
