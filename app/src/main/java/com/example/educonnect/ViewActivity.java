package com.example.educonnect;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;
    private StudentMaterialAdapter adapter;
    private List<DocumentSnapshot> materialList = new ArrayList<>();
    private TextView tvHeader, tvNoData;

    // default collection
    private String type = "study_materials";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_material);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerMaterials);
        tvHeader = findViewById(R.id.tvHeader);
        tvNoData = findViewById(R.id.tvNoData); // 👈 added reference

        if (getIntent().hasExtra("type")) {
            type = getIntent().getStringExtra("type");
        }

        // set heading
        switch (type) {
            case "question_papers":
                tvHeader.setText("📄 Question Papers");
                break;
            case "timetables":
                tvHeader.setText("🕒 Timetables");
                break;
            default:
                tvHeader.setText("📚 Study Materials");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentMaterialAdapter(this, materialList);
        recyclerView.setAdapter(adapter);

        loadMaterials();
        hideAdminUploadFields();
    }

    private void loadMaterials() {
        firestore.collection(type)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {
                    materialList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        materialList.add(doc);
                    }

                    adapter.notifyDataSetChanged();

                    // 👇 If no material found, show message
                    if (materialList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);

                        if (type.equals("timetables")) {
                            tvNoData.setText("No Timetable Available Yet 📅");
                        } else if (type.equals("question_papers")) {
                            tvNoData.setText("No Question Papers Uploaded 📝");
                        } else {
                            tvNoData.setText("No Study Material Available 📚");
                        }
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void hideAdminUploadFields() {
        int[] idsToHide = {
                R.id.etTitle,
                R.id.etDescription,
                R.id.etDriveLink,
                R.id.btnUpload
        };

        for (int id : idsToHide) {
            View view = findViewById(id);
            if (view != null) view.setVisibility(View.GONE);
        }

        View listHeader = findViewById(R.id.tvListHeader);
        if (listHeader != null) listHeader.setVisibility(View.VISIBLE);
    }
}
