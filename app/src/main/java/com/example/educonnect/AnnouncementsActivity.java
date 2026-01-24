package com.example.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private FloatingActionButton fabAdd;
    private LinearLayout mainContainer;

    private FirebaseFirestore db;
    private AnnouncementsAdapter adapter;
    private List<Announcement> announcementList;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        // 🔹 Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(" Announcements ");

        // 🔹 UI references
        recyclerView = findViewById(R.id.recyclerViewAnnouncements);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        fabAdd = findViewById(R.id.fabAddAnnouncement);
        mainContainer = findViewById(R.id.mainContainer);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementList = new ArrayList<>();
        adapter = new AnnouncementsAdapter(this, announcementList, isAdmin);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 🔍 Determine user role
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        Object roleObj = doc.get("role");
                        String role = (roleObj != null) ? roleObj.toString() : "student";
                        isAdmin = role.equalsIgnoreCase("admin");
                    } else {
                        isAdmin = false;
                    }

                    // ✅ Show FAB for admin only
                    fabAdd.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

                    adapter = new AnnouncementsAdapter(this, announcementList, isAdmin);
                    recyclerView.setAdapter(adapter);
                    loadAnnouncements();
                })
                .addOnFailureListener(e -> {
                    isAdmin = false;
                    fabAdd.setVisibility(View.GONE);
                    adapter = new AnnouncementsAdapter(this, announcementList, isAdmin);
                    recyclerView.setAdapter(adapter);
                    loadAnnouncements();
                });

        // ➕ Add announcement (admins only)
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddAnnouncementActivity.class))
        );
    }

    // 🔄 Load announcements
    private void loadAnnouncements() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);

        db.collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    announcementList.clear();
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (DocumentSnapshot doc : snapshot) {
                                Announcement a = doc.toObject(Announcement.class);
                                if (a != null) announcementList.add(a);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            tvNoData.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tvNoData.setText("Failed to load announcements ❌");
                        tvNoData.setVisibility(View.VISIBLE);
                    }

                    // ✅ Center announcements for both roles
                    recyclerView.post(() -> {
                        int totalHeight = recyclerView.computeVerticalScrollRange();
                        int parentHeight = recyclerView.getHeight();
                        if (totalHeight < parentHeight) {
                            recyclerView.setPadding(0, (parentHeight - totalHeight) / 2, 0, 0);
                        } else {
                            recyclerView.setPadding(0, 0, 0, 0);
                        }
                    });
                });
    }
}
