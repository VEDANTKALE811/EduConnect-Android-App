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

public class StudentDashboardActivity extends AppCompatActivity {

    // --- UI Elements ---
    private MaterialCardView cardChat, cardAnnouncements, cardReminders, cardProfile,
            cardQuestionPapers, cardStudyMaterial, cardTimetable, cardQuiz,
            cardJoinGroup, cardViewGroups, cardAIQuery;
    private TextView tvWelcome;
    private ShapeableImageView ivLogo;
    private MaterialButton btnLogout, btnDeleteAccount;

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ Force app into Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // --- Firebase Setup ---
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // --- Initialize Views ---
        ivLogo = findViewById(R.id.ivLogo);
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        cardChat = findViewById(R.id.cardChat);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardReminders = findViewById(R.id.cardReminders);
        cardProfile = findViewById(R.id.cardProfile);
        cardQuestionPapers = findViewById(R.id.cardQuestionPapers);
        cardStudyMaterial = findViewById(R.id.cardStudyMaterial);
        cardTimetable = findViewById(R.id.cardTimetable);
        cardQuiz = findViewById(R.id.cardQuiz);
        cardJoinGroup = findViewById(R.id.cardJoinGroup);
        cardViewGroups = findViewById(R.id.cardViewGroups);
        cardAIQuery = findViewById(R.id.cardAIQuery);

        // --- Animation ---
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivLogo.startAnimation(fadeIn);
        tvWelcome.startAnimation(fadeIn);

        // --- Load Welcome Message ---
        showWelcomeMessage();

        // --- Setup Navigation ---
        setupNavigation();

        // --- Logout & Delete Account ---
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    /**
     * 🎓 Display welcome message for student
     */
    private void showWelcomeMessage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            tvWelcome.setText(name != null && !name.isEmpty()
                    ? "Welcome, " + name + " 👋"
                    : (email != null ? "Welcome, " + email + " 👋" : "Welcome Student 👋"));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    /**
     * 🧭 Set up navigation to all sections
     */
    private void setupNavigation() {
        cardProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        cardChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        // 📚 Study Materials
        cardStudyMaterial.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewActivity.class);
            i.putExtra("type", "study_materials");
            startActivity(i);
        });

        // 📄 Question Papers
        cardQuestionPapers.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewActivity.class);
            i.putExtra("type", "question_papers");
            startActivity(i);
        });

        // 🕒 Timetables
        cardTimetable.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewActivity.class);
            i.putExtra("type", "timetables");
            startActivity(i);
        });

        // 📢 Announcements
        cardAnnouncements.setOnClickListener(v -> startActivity(new Intent(this, AnnouncementsActivity.class)));

        // ⏰ Reminders
        cardReminders.setOnClickListener(v -> startActivity(new Intent(this, RemindersActivity.class)));

        // 🧠 Quiz
        cardQuiz.setOnClickListener(v -> startActivity(new Intent(this, ViewQuizActivity.class)));

        // 👥 Join Group
        cardJoinGroup.setOnClickListener(v -> startActivity(new Intent(this, JoinGroupActivity.class)));

        // 👥 View Groups
        cardViewGroups.setOnClickListener(v -> startActivity(new Intent(this, ViewGroupsActivity.class)));

        // 🤖 AI Assistant
        cardAIQuery.setOnClickListener(v -> startActivity(new Intent(this, AIQueryActivity.class)));
    }

    /**
     * 🚪 Logout Dialog
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setIcon(android.R.drawable.ic_lock_power_off)
                .setPositiveButton("Logout", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * ❌ Delete Account Dialog
     */
    private void showDeleteAccountDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Account Permanently?")
                .setMessage("This will permanently delete your EduConnect account and data. This cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String uid = user.getUid();
                    firestore.collection("users").document(uid)
                            .delete()
                            .addOnSuccessListener(aVoid -> user.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this, LoginActivity.class));
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
