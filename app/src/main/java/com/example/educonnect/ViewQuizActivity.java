package com.example.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ViewQuizActivity extends AppCompatActivity {

    private RecyclerView recyclerViewQuizzes, recyclerViewResults;
    private QuizListAdapter quizAdapter;
    private ResultAdapter resultAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<DocumentSnapshot> quizList = new ArrayList<>();
    private List<DocumentSnapshot> resultList = new ArrayList<>();
    private boolean isAdmin = false;
    private LinearLayout resultSection;
    private TextView tvNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_quiz);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerViewQuizzes = findViewById(R.id.recyclerViewQuiz);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        resultSection = findViewById(R.id.resultSection);
        tvNoResults = findViewById(R.id.tvNoResults);

        recyclerViewQuizzes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));

        String uid = auth.getUid();
        if (uid != null && uid.equals("YOUR_ADMIN_UID")) {
            isAdmin = true;
        }

        quizAdapter = new QuizListAdapter(
                quizList,
                this::openQuiz,
                isAdmin ? this::confirmDelete : null
        );
        recyclerViewQuizzes.setAdapter(quizAdapter);

        resultAdapter = new ResultAdapter(resultList);
        recyclerViewResults.setAdapter(resultAdapter);

        loadQuizzes();
        loadResults();
    }

    private void loadQuizzes() {
        db.collection("quizzes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    quizList.clear();
                    quizList.addAll(snapshots.getDocuments());
                    quizAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading quizzes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadResults() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("results")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    resultList.clear();
                    resultList.addAll(snapshots.getDocuments());
                    resultAdapter.notifyDataSetChanged();

                    if (resultList.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        resultSection.setVisibility(View.GONE);
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        resultSection.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading results: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openQuiz(String quizId) {
        db.collection("quizzes").document(quizId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String title = snapshot.getString("title");
                        Intent intent = new Intent(this, AttemptQuizActivity.class);
                        intent.putExtra("quizId", quizId);
                        intent.putExtra("quizTitle", title);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Quiz not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete(String quizId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Quiz")
                .setMessage("Delete this quiz permanently?")
                .setPositiveButton("Delete", (d, w) -> deleteQuiz(quizId))
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    private void deleteQuiz(String quizId) {
        db.collection("quizzes").document(quizId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadQuizzes();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
