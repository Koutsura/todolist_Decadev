package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private ArrayList<Task> tasks;

    public TaskAdapter(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());

        // Menampilkan waktu pengingat yang sudah diformat
        long reminderTime = task.getReminderTime();

        // Menggunakan SimpleDateFormat untuk format waktu (HH:mm)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String reminderTimeString = sdf.format(new Date(reminderTime));  // Mengonversi timestamp ke format waktu
        holder.taskReminderTime.setText("Reminder at: " + reminderTimeString);  // Menampilkan waktu pengingat
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskReminderTime;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskReminderTime = itemView.findViewById(R.id.taskReminderTime); // Menambahkan TextView untuk reminder time
        }
    }
}
