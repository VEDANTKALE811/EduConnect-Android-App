package com.example.educonnect;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
    private ImageView ivLogo;
    private MaterialCardView cardLogin;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transparent status bar
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setContentView(R.layout.activity_login);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Init views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvHero = findViewById(R.id.tvHero);
        progressBar = findViewById(R.id.login_progress_bar);
        ivLogo = findViewById(R.id.ivLogo);
        cardLogin = findViewById(R.id.cardLogin);

        // Animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        ivLogo.startAnimation(fadeIn);
        tvHero.startAnimation(fadeIn);
        cardLogin.startAnimation(slideUp);

        // Click listeners
        btnLogin.setOnClickListener(v -> loginUser());
        tvSignUpLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class))
        );
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
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
                            msg = "No account found";
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
