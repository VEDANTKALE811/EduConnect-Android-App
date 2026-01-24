package com.example.educonnect;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemindersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoReminders;
    private ImageButton btnAdd;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Reminder> reminderList;
    private RemindersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        recyclerView = findViewById(R.id.recyclerViewReminders);
        progressBar = findViewById(R.id.progressBar);
        tvNoReminders = findViewById(R.id.tvNoReminders);
        btnAdd = findViewById(R.id.btnAddReminder);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        reminderList = new ArrayList<>();
        adapter = new RemindersAdapter(reminderList, this::deleteReminder);
        recyclerView.setAdapter(adapter);

        loadReminders();

        btnAdd.setOnClickListener(v -> showAddReminderDialog());
    }

    private void loadReminders() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoReminders.setVisibility(View.GONE);

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("reminders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        reminderList.clear();
                        task.getResult().forEach(doc -> {
                            Reminder r = doc.toObject(Reminder.class);
                            r.setId(doc.getId());
                            reminderList.add(r);
                        });

                        if (reminderList.isEmpty()) {
                            tvNoReminders.setVisibility(View.VISIBLE);
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        tvNoReminders.setText("Failed to load reminders ❌");
                        tvNoReminders.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showAddReminderDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_reminder, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etTime = dialogView.findViewById(R.id.etTime);

        Calendar calendar = Calendar.getInstance();

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, day) -> {
                etDate.setText(day + "/" + (month + 1) + "/" + year);
                calendar.set(year, month, day);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        etTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hour, minute) -> {
                etTime.setText(String.format("%02d:%02d", hour, minute));
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Reminder")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String time = etTime.getText().toString().trim();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
                        return;
                    }

                    saveReminder(title, date, time);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveReminder(String title, String date, String time) {
        Map<String, Object> reminder = new HashMap<>();
        reminder.put("title", title);
        reminder.put("date", date);
        reminder.put("time", time);
        reminder.put("userId", mAuth.getCurrentUser().getUid());

        db.collection("reminders")
                .add(reminder)
                .addOnSuccessListener(docRef -> loadReminders());
    }

    private void deleteReminder(Reminder reminder) {
        db.collection("reminders").document(reminder.getId())
                .delete()
                .addOnSuccessListener(aVoid -> loadReminders());
    }
}
