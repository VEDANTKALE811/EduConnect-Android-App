package com.example.educonnect;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class ViewGroupsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<GroupModel> groupList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_groups);

        recyclerView = findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        groupList = new ArrayList<>();

        adapter = new GroupAdapter(this, groupList, new GroupAdapter.OnGroupClickListener() {
            @Override
            public void onViewMembers(GroupModel group) {
                Intent intent = new Intent(ViewGroupsActivity.this, ViewMembersActivity.class);
                intent.putExtra("groupId", group.getId());
                intent.putExtra("groupName", group.getGroupName());
                startActivity(intent);
            }

            @Override
            public void onDeleteGroup(GroupModel group) {
                confirmDelete(group);
            }
        });

        recyclerView.setAdapter(adapter);
        loadGroups();
    }

    private void loadGroups() {
        db.collection("groups")
                .whereArrayContains("members", auth.getUid())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading groups", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    groupList.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            GroupModel g = doc.toObject(GroupModel.class);
                            if (g != null) {
                                g.setId(doc.getId());
                                groupList.add(g);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void confirmDelete(GroupModel group) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete \"" + group.getGroupName() + "\"?")
                .setPositiveButton("Delete", (d, w) -> deleteGroup(group))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGroup(GroupModel group) {
        db.collection("groups").document(group.getId())
                .delete()
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
