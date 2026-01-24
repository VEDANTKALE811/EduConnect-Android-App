package com.example.educonnect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class QuestionPapersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuestionPaperAdapter adapter;
    private FloatingActionButton fabUpload;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_papers);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        recyclerView = findViewById(R.id.recyclerViewPapers);
        fabUpload = findViewById(R.id.fabUpload);
        progressBar = findViewById(R.id.progressBar);

        // Hide by default; will be shown if user is admin
        fabUpload.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("question_papers");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");

        // File picker
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) uploadFile(uri);
                });

        // Back nav
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        checkUserRoleAndSetup();
        loadQuestionPapers();
    }

    private void checkUserRoleAndSetup() {
        String uid = auth.getUid();
        if (uid == null) {
            fabUpload.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        if ("admin".equals(role)) {
                            // show and enable upload
                            fabUpload.setVisibility(View.VISIBLE);
                            fabUpload.setOnClickListener(v -> filePickerLauncher.launch("application/pdf"));
                        } else {
                            fabUpload.setVisibility(View.GONE);
                        }
                    } else {
                        fabUpload.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    fabUpload.setVisibility(View.GONE);
                });
    }

    private void loadQuestionPapers() {
        progressBar.setVisibility(View.VISIBLE);
        Query query = db.collection("question_papers").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<QuestionPaperModel> options =
                new FirestoreRecyclerOptions.Builder<QuestionPaperModel>()
                        .setQuery(query, QuestionPaperModel.class)
                        .build();

        adapter = new QuestionPaperAdapter(options);
        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.GONE);
    }

    private void uploadFile(Uri pdfUri) {
        progressDialog.show();
        String fileName = "QP_" + System.currentTimeMillis() + ".pdf";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> savePaperToFirestore(fileName, uri.toString()))
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void savePaperToFirestore(String title, String downloadUrl) {
        Map<String, Object> paper = new HashMap<>();
        paper.put("title", title);
        paper.put("url", downloadUrl);
        paper.put("uploadedBy", auth.getUid());
        paper.put("timestamp", System.currentTimeMillis());

        db.collection("question_papers").add(paper)
                .addOnSuccessListener(docRef -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    // adapter will pick new data if using snapshot listener / FirestoreRecyclerAdapter
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
