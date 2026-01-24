package com.example.educonnect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyMaterialActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabUpload;
    private StudyMaterialAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private List<StudyMaterialModel> materialList;
    private ProgressDialog progressDialog;

    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_material);

        toolbar = findViewById(R.id.toolbarStudy);
        recyclerView = findViewById(R.id.recyclerStudy);
        progressBar = findViewById(R.id.progressBar);
        fabUpload = findViewById(R.id.fabUploadStudy);

        // hide by default until role check
        fabUpload.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("study_materials");
        materialList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudyMaterialAdapter(materialList, this::openMaterial);
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading material...");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) uploadFile(uri);
                });

        checkUserRole();
        loadMaterials();
    }

    private void checkUserRole() {
        String uid = auth.getUid();
        if (uid == null) {
            fabUpload.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        if ("admin".equals(role)) {
                            fabUpload.setVisibility(View.VISIBLE);
                            fabUpload.setOnClickListener(v -> filePickerLauncher.launch("application/pdf"));
                        } else {
                            fabUpload.setVisibility(View.GONE);
                        }
                    } else {
                        fabUpload.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> fabUpload.setVisibility(View.GONE));
    }

    private void loadMaterials() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("StudyMaterials")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        materialList.clear();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            StudyMaterialModel material = doc.toObject(StudyMaterialModel.class);
                            materialList.add(material);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadFile(Uri pdfUri) {
        progressDialog.show();
        String fileName = "Material_" + System.currentTimeMillis() + ".pdf";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveToFirestore(fileName, uri.toString()))
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String title, String url) {
        Map<String, Object> material = new HashMap<>();
        material.put("title", title);
        material.put("url", url);
        material.put("uploadedBy", auth.getUid());
        material.put("timestamp", System.currentTimeMillis());

        db.collection("StudyMaterials")
                .add(material)
                .addOnSuccessListener(docRef -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Material uploaded successfully!", Toast.LENGTH_SHORT).show();
                    loadMaterials();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openMaterial(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show();
        }
    }
}
