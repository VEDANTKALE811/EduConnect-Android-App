package com.example.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword, etClassYear;
    private MaterialButton btnSignUp;
    private TextView tvLoginLink;
    private RadioGroup rgAccountType;
    private RadioButton rbStudent, rbAdmin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etClassYear = findViewById(R.id.etClassYear);

        rgAccountType = findViewById(R.id.rgAccountType);
        rbStudent = findViewById(R.id.rbStudent);
        rbAdmin = findViewById(R.id.rbAdmin);

        btnSignUp = findViewById(R.id.btnSignUp);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Sign Up click
        btnSignUp.setOnClickListener(v -> registerUser());

        // Login link click
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String classYear = etClassYear.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Full Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Account type
        int selectedId = rgAccountType.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select account type", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRole = findViewById(selectedId);
        String role = selectedRole.getText().toString().toLowerCase(); // "student" or "admin"

        // Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("classYear", classYear);
                        user.put("role", role);

                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                    // Redirect based on role
                                    if (role.equals("admin")) {
                                        startActivity(new Intent(SignUpActivity.this, AdminDashboardActivity.class));
                                    } else {
                                        startActivity(new Intent(SignUpActivity.this, StudentDashboardActivity.class));
                                    }
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(SignUpActivity.this, "Error saving user info: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );

                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
