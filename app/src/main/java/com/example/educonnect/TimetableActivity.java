package com.example.educonnect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableActivity extends AppCompatActivity {

    private LinearLayout layoutTimetableContainer;
    private ProgressBar progressBar;
    private FloatingActionButton fabUpload;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        MaterialToolbar toolbar = findViewById(R.id.toolbarTimetable);
        layoutTimetableContainer = findViewById(R.id.layoutTimetableContainer);
        progressBar = findViewById(R.id.progressBar);
        fabUpload = findViewById(R.id.fabUploadTimetable);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("timetables");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading timetable...");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) uploadTimetable(uri);
                });

        checkUserRole();  // Show FAB for admins only
        loadTimetables();
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

    private void loadTimetables() {
        progressBar.setVisibility(View.VISIBLE);
        layoutTimetableContainer.removeAllViews();

        db.collection("timetable")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();

                        if (docs.isEmpty()) {
                            Toast.makeText(this, "No timetable available", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DocumentSnapshot doc : docs) {
                            String title = doc.getString("title");
                            String url = doc.getString("url");

                            addTimetableCard(title, url);
                        }
                    } else {
                        Toast.makeText(this, "Failed to load timetable", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addTimetableCard(String title, String url) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(20f);
        card.setCardElevation(8f);
        card.setUseCompatPadding(true);
        card.setContentPadding(32, 24, 32, 24);
        card.setCardBackgroundColor(getColor(android.R.color.white));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTextColor(getColor(android.R.color.black));
        titleView.setPadding(0, 0, 0, 8);

        TextView linkView = new TextView(this);
        linkView.setText("Open Timetable");
        linkView.setTextSize(14);
        linkView.setTextColor(getColor(android.R.color.holo_purple));
        linkView.setOnClickListener(v -> openTimetable(url));

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.addView(titleView);
        innerLayout.addView(linkView);

        card.addView(innerLayout);
        layoutTimetableContainer.addView(card);
    }

    private void uploadTimetable(Uri pdfUri) {
        progressDialog.show();
        String fileName = "Timetable_" + System.currentTimeMillis() + ".pdf";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveToFirestore(fileName, uri.toString())))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String title, String url) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("url", url);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("timetable")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Timetable uploaded successfully!", Toast.LENGTH_SHORT).show();
                    loadTimetables();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openTimetable(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
