package com.example.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    // --- UI Elements ---
    private MaterialCardView cardAddMaterial, cardAddQuestion, cardAddQuiz, cardAnnouncements, cardTimetable;
    private MaterialCardView cardChat, cardCreateGroup, cardProfile, cardViewGroups, cardReminders, cardAIQuery;
    private TextView tvWelcome, tvSubtitle;
    private ShapeableImageView ivLogo;
    private MaterialButton btnLogout, btnDeleteAccount;

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ Always use Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // --- Firebase Setup ---
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // --- Initialize Views ---
        ivLogo = findViewById(R.id.ivLogo);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        cardAddMaterial = findViewById(R.id.cardAddMaterial);
        cardAddQuestion = findViewById(R.id.cardAddQuestion);
        cardAddQuiz = findViewById(R.id.cardAddQuiz);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardChat = findViewById(R.id.cardChat);
        cardCreateGroup = findViewById(R.id.cardCreateGroup);
        cardProfile = findViewById(R.id.cardProfile);
        cardViewGroups = findViewById(R.id.cardViewGroups);
        cardReminders = findViewById(R.id.cardReminders);
        cardTimetable = findViewById(R.id.cardTimetable);
        cardAIQuery = findViewById(R.id.cardAIQuery);

        // --- Animation ---
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivLogo.startAnimation(fadeIn);

        // --- Load Admin Data ---
        loadAdminData();

        // --- Setup Navigation ---
        setupNavigation();

        // --- Logout & Delete Listeners ---
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void loadAdminData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            tvWelcome.setText("Welcome, Admin 👑");
            tvSubtitle.setText("Manage your EduConnect portal");
            return;
        }

        String uid = currentUser.getUid();
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvWelcome.setText("Welcome, " + name + " 👑");
                        } else {
                            tvWelcome.setText("Welcome, Admin 👑");
                        }
                    } else {
                        tvWelcome.setText("Welcome, Admin 👑");
                    }
                    tvSubtitle.setText("Manage your EduConnect portal");
                })
                .addOnFailureListener(e -> {
                    tvWelcome.setText("Welcome, Admin 👑");
                    tvSubtitle.setText("Manage your EduConnect portal");
                });
    }

    private void setupNavigation() {
        // --- Navigation to other features ---
        cardAnnouncements.setOnClickListener(v ->
                startActivity(new Intent(this, AnnouncementsActivity.class)));

        cardChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));

        cardCreateGroup.setOnClickListener(v ->
                startActivity(new Intent(this, CreateGroupActivity.class)));

        cardViewGroups.setOnClickListener(v ->
                startActivity(new Intent(this, ViewGroupsActivity.class)));

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        cardReminders.setOnClickListener(v ->
                startActivity(new Intent(this, RemindersActivity.class)));

        cardAIQuery.setOnClickListener(v ->
                startActivity(new Intent(this, AIQueryActivity.class)));

        // --- Upload Material ---
        cardAddMaterial.setOnClickListener(v -> {
            Intent i = new Intent(this, UploadMaterialActivity.class);
            i.putExtra("type", "study_materials");
            startActivity(i);
        });

        cardAddQuestion.setOnClickListener(v -> {
            Intent i = new Intent(this, UploadMaterialActivity.class);
            i.putExtra("type", "question_papers");
            startActivity(i);
        });

        cardTimetable.setOnClickListener(v -> {
            Intent i = new Intent(this, UploadMaterialActivity.class);
            i.putExtra("type", "timetables");
            startActivity(i);
        });

        // --- Quiz Section ---
        cardAddQuiz.setOnClickListener(v ->
                startActivity(new Intent(this, QuizActivity.class)));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out of EduConnect?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_lock_power_off)
                .show();
    }

    private void showDeleteAccountDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Account Permanently?")
                .setMessage("This will permanently delete your account and data. This action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String uid = user.getUid();

                    firestore.collection("users").document(uid)
                            .delete()
                            .addOnSuccessListener(aVoid ->
                                    user.delete().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(this, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Failed to delete from Firebase Auth.", Toast.LENGTH_SHORT).show();
                                        }
                                    }))
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to delete Firestore data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
