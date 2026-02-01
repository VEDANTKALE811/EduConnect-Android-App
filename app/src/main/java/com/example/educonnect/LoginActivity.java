package com.example.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    // Views (MATCH NEW XML)
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvSignUpLink, tvForgotPassword, tvHero;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Init views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);
        tvHero = findViewById(R.id.tvHero);
        progressBar = findViewById(R.id.login_progress_bar);

        // Click listeners
        btnLogin.setOnClickListener(v -> loginUser());

        tvSignUpLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        String role = doc.getString("role");

                                        if ("admin".equalsIgnoreCase(role)) {
                                            startActivity(new Intent(this, AdminDashboardActivity.class));
                                        } else {
                                            startActivity(new Intent(this, StudentDashboardActivity.class));
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        String msg;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            msg = "No account found with this email";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            msg = "Incorrect password";
                        } catch (Exception e) {
                            msg = "Login failed";
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        EditText input = new EditText(this);
        input.setHint("Enter registered email");
        builder.setView(input);

        builder.setPositiveButton("Send", (d, w) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(a ->
                                Toast.makeText(this, "Reset email sent", Toast.LENGTH_LONG).show());
            }
        });

        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }
}
