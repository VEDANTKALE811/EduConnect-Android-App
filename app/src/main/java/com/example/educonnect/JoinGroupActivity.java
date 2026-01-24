package com.example.educonnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class JoinGroupActivity extends AppCompatActivity {

    private TextInputEditText etGroupCode;
    private MaterialButton btnJoin;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        // 🔹 Initialize UI elements
        etGroupCode = findViewById(R.id.etGroupCode);
        btnJoin = findViewById(R.id.btnJoin);
        progressBar = findViewById(R.id.progressBar);

        // 🔹 Firebase setup
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnJoin.setOnClickListener(v -> joinGroup());
    }

    /**
     * ✅ Allows a student to join a group using a code.
     */
    private void joinGroup() {
        String code = etGroupCode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            etGroupCode.setError("Enter group code");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnJoin.setEnabled(false);

        // 🔹 Search Firestore for group with matching code
        db.collection("groups")
                .whereEqualTo("groupCode", code)
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);

                    if (snapshots.isEmpty()) {
                        Toast.makeText(this, "❌ Invalid code. Try again!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");

                        if (members != null && members.contains(auth.getUid())) {
                            Toast.makeText(this, "Already joined this group!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 🔹 Use FieldValue.arrayUnion for safe update
                        db.collection("groups").document(doc.getId())
                                .update("members", FieldValue.arrayUnion(auth.getUid()))
                                .addOnSuccessListener(aVoid -> {
                                    // 🔹 Also add this group under the user's document (optional but useful)
                                    db.collection("users").document(auth.getUid())
                                            .update("groups", FieldValue.arrayUnion(doc.getId()));

                                    Toast.makeText(this, "✅ Successfully joined the group!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "⚠️ Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
