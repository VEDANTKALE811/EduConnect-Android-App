package com.example.educonnect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private ImageView btnChangePhoto;
    private TextView tvUserEmail;
    private TextInputEditText etName, etClass, etDept, etCollege, etPhone, etBio;
    private FloatingActionButton btnSave;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_modern);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        profileImage = findViewById(R.id.profileImage);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        etName = findViewById(R.id.etName);
        etClass = findViewById(R.id.etClass);
        etDept = findViewById(R.id.etDept);
        etCollege = findViewById(R.id.etCollege);
        etPhone = findViewById(R.id.etPhone);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Updating profile...");

        // ✅ Set current user's email
        if (auth.getCurrentUser() != null) {
            tvUserEmail.setText(auth.getCurrentUser().getEmail());
        }

        loadUserData();

        btnChangePhoto.setOnClickListener(v -> chooseImage());
        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(uid);

        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                etName.setText(document.getString("name"));
                etClass.setText(document.getString("class"));
                etDept.setText(document.getString("department"));
                etCollege.setText(document.getString("college"));
                etPhone.setText(document.getString("phone"));
                etBio.setText(document.getString("bio"));

                String imgUrl = document.getString("profileImage");
                if (imgUrl != null && !imgUrl.isEmpty()) {
                    Glide.with(this)
                            .load(imgUrl)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .into(profileImage);
                }
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void saveUserData() {
        progressDialog.show();

        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(uid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etName.getText().toString().trim());
        updates.put("class", etClass.getText().toString().trim());
        updates.put("department", etDept.getText().toString().trim());
        updates.put("college", etCollege.getText().toString().trim());
        updates.put("phone", etPhone.getText().toString().trim());
        updates.put("bio", etBio.getText().toString().trim());
        updates.put("email", auth.getCurrentUser().getEmail());

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(uid + ".jpg");
            fileRef.putFile(imageUri)
                    .continueWithTask(task -> fileRef.getDownloadUrl())
                    .addOnSuccessListener(uri -> {
                        updates.put("profileImage", uri.toString());
                        updateFirestore(userRef, updates);
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateFirestore(userRef, updates);
        }
    }

    private void updateFirestore(DocumentReference ref, Map<String, Object> updates) {
        ref.update(updates)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "❌ Failed to update profile!", Toast.LENGTH_SHORT).show();
                });
    }
}
