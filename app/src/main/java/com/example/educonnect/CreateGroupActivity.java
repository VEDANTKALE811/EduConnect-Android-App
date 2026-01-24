package com.example.educonnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.TextView;
import java.util.*;

public class CreateGroupActivity extends AppCompatActivity {

    private TextInputEditText etGroupName, etDescription;
    private MaterialButton btnGenerateCode, btnCreateGroup;
    private ProgressBar progressBar;
    private CardView cardCode;
    private com.google.android.material.textview.MaterialTextView tvCode;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String generatedCode = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // 🔹 Initialize UI elements
        etGroupName = findViewById(R.id.etGroupName);
        etDescription = findViewById(R.id.etDescription);
        btnGenerateCode = findViewById(R.id.btnGenerateCode);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        progressBar = findViewById(R.id.progressBar);
        cardCode = findViewById(R.id.cardCode);
        tvCode = findViewById(R.id.tvCode);

        // 🔹 Firebase setup
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔹 Generate random 6-character group code
        btnGenerateCode.setOnClickListener(v -> {
            generatedCode = generateGroupCode();
            tvCode.setText(generatedCode);
            cardCode.setVisibility(View.VISIBLE);
        });

        // 🔹 Create new group in Firestore
        btnCreateGroup.setOnClickListener(v -> createGroup());
    }

    /**
     * ✅ Creates a group in Firestore and links it to admin user
     */
    private void createGroup() {
        String name = etGroupName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etGroupName.setError("Enter group name");
            return;
        }
        if (TextUtils.isEmpty(generatedCode)) {
            Toast.makeText(this, "Generate group code first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreateGroup.setEnabled(false);

        // 🔹 Create group data map
        Map<String, Object> group = new HashMap<>();
        group.put("groupName", name);
        group.put("description", desc);
        group.put("groupCode", generatedCode);
        group.put("createdBy", auth.getUid());
        group.put("ownerUid", auth.getUid()); // optional but useful for filtering
        group.put("createdAt", new Date());
        group.put("members", new ArrayList<>(Collections.singletonList(auth.getUid())));

        // 🔹 Save group in Firestore
        db.collection("groups")
                .add(group)
                .addOnSuccessListener(doc -> {
                    // ✅ Update admin's user document with this groupId
                    db.collection("users").document(auth.getUid())
                            .update("groups", FieldValue.arrayUnion(doc.getId()))
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "✅ Group Created & Linked Successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "⚠️ Group saved but user link failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateGroup.setEnabled(true);
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * 🎲 Generates random 6-character group code (e.g. A1B2C3)
     */
    private String generateGroupCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++)
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        return code.toString();
    }
}
