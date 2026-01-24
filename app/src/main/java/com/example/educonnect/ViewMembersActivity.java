package com.example.educonnect;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewMembersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private List<MemberModel> memberList;
    private FirebaseFirestore db;
    private String groupId, groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_members);

        recyclerView = findViewById(R.id.recyclerViewMembers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        setTitle("Members of " + groupName);

        memberList = new ArrayList<>();
        adapter = new MemberAdapter(this, memberList);
        recyclerView.setAdapter(adapter);

        loadMembers();
    }

    private void loadMembers() {
        db.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> memberIds = (List<String>) doc.get("members");
                    if (memberIds == null || memberIds.isEmpty()) {
                        Toast.makeText(this, "No members found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    memberList.clear();

                    for (String uid : memberIds) {
                        db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String name = userDoc.getString("name");
                                        String email = userDoc.getString("email");
                                        String image = userDoc.getString("profileImage");

                                        memberList.add(new MemberModel(uid, name, email, image));
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error loading user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
