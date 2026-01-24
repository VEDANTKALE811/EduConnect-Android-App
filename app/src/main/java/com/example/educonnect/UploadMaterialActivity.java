package com.example.educonnect;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;                     // ✅ add this
import android.widget.AdapterView;           // ✅ add this
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;


public class UploadMaterialActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDriveLink;
    private MaterialButton btnUpload;
    private TextView tvHeader;
    private RecyclerView recyclerView;
    private Spinner spinnerGroup;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private MaterialItemAdapter adapter;
    private List<DocumentSnapshot> materialList = new ArrayList<>();
    private List<String> groupNames = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();

    private String type = "study_materials";
    private String selectedGroupId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_material);

        // 🔹 Initialize Views
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDriveLink = findViewById(R.id.etDriveLink);
        btnUpload = findViewById(R.id.btnUpload);
        tvHeader = findViewById(R.id.tvHeader);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        recyclerView = findViewById(R.id.recyclerMaterials);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (getIntent().hasExtra("type")) {
            type = getIntent().getStringExtra("type");
        }

        switch (type) {
            case "question_papers":
                tvHeader.setText("📄 Upload Question Papers");
                break;
            case "timetables":
                tvHeader.setText("🕒 Upload Timetables");
                break;
            default:
                tvHeader.setText("📚 Upload Study Material");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialItemAdapter(this, materialList, type);
        recyclerView.setAdapter(adapter);

        // 🔹 Load groups created by the logged-in admin
        loadGroups();

        btnUpload.setOnClickListener(v -> uploadMaterial());
    }

    private void loadGroups() {
        firestore.collection("groups")
                .whereEqualTo("createdBy", auth.getUid()) // only admin's groups
                .get()
                .addOnSuccessListener(query -> {
                    groupNames.clear();
                    groupIds.clear();

                    for (DocumentSnapshot doc : query) {
                        groupNames.add(doc.getString("groupName"));
                        groupIds.add(doc.getId());
                    }

                    if (groupNames.isEmpty()) {
                        groupNames.add("No groups available");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, groupNames);
                    spinnerGroup.setAdapter(adapter);

                    spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (!groupIds.isEmpty()) {
                                selectedGroupId = groupIds.get(position);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedGroupId = null;
                        }
                    });
                });
    }

    private void uploadMaterial() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String link = etDriveLink.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(link)) {
            Toast.makeText(this, "Title and link are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedGroupId == null) {
            Toast.makeText(this, "Please select a group", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.show();

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", desc);
        data.put("driveLink", link);
        data.put("groupId", selectedGroupId); // ✅ Target group
        data.put("uploadedBy", auth.getUid());
        data.put("timestamp", System.currentTimeMillis());

        firestore.collection(type)
                .add(data)
                .addOnSuccessListener(doc -> {
                    dialog.dismiss();
                    Toast.makeText(this, "✅ Uploaded successfully!", Toast.LENGTH_SHORT).show();
                    loadMaterials();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMaterials() {
        firestore.collection(type)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    materialList.clear();
                    for (DocumentSnapshot doc : query) {
                        materialList.add(doc);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
