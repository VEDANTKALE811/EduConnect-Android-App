package com.example.educonnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttemptQuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvCounter;
    private RadioGroup rgOptions;
    private Button btnNext;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<QuestionModel> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private String quizId;
    private String quizTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attempt_quiz);

        // ✅ Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ✅ Initialize UI elements
        tvQuestion = findViewById(R.id.tvQuestion);
        tvCounter = findViewById(R.id.tvCounter);
        rgOptions = findViewById(R.id.rgOptions);
        btnNext = findViewById(R.id.btnNext);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        quizId = getIntent().getStringExtra("quizId");
        quizTitle = getIntent().getStringExtra("quizTitle"); // optional

        loadQuiz();

        btnNext.setOnClickListener(v -> {
            if (rgOptions.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            checkAnswer();
            currentIndex++;

            if (currentIndex < questionList.size()) {
                showQuestion();
            } else {
                saveResultToFirestore(); // ✅ Save result before showing dialog
            }
        });
    }

    private void loadQuiz() {
        db.collection("quizzes").document(quizId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        List<Map<String, Object>> questions =
                                (List<Map<String, Object>>) snapshot.get("questions");

                        questionList.clear();
                        for (Map<String, Object> q : questions) {
                            questionList.add(new QuestionModel(
                                    (String) q.get("question"),
                                    (List<String>) q.get("options"),
                                    (String) q.get("answer")
                            ));
                        }
                        showQuestion();
                    } else {
                        Toast.makeText(this, "Quiz not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showQuestion() {
        rgOptions.removeAllViews();

        QuestionModel q = questionList.get(currentIndex);
        tvQuestion.setText(q.getQuestion());
        tvCounter.setText("Question " + (currentIndex + 1) + " / " + questionList.size());

        for (String option : q.getOptions()) {
            RadioButton rb = new RadioButton(this);
            rb.setText(option);
            rb.setTextSize(16);
            rb.setPadding(12, 8, 12, 8);
            rgOptions.addView(rb);
        }
    }

    private void checkAnswer() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        RadioButton selected = findViewById(selectedId);
        String answer = selected.getText().toString();

        if (answer.equalsIgnoreCase(questionList.get(currentIndex).getAnswer())) {
            score++;
        }
    }

    // ✅ Save result to Firestore after finishing
    private void saveResultToFirestore() {
        String userId = auth.getUid();
        if (userId == null) return;

        long timestamp = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        result.put("quizId", quizId);
        result.put("quizTitle", quizTitle != null ? quizTitle : "Quiz");
        result.put("userId", userId);
        result.put("score", score);
        result.put("total", questionList.size());
        result.put("timestamp", timestamp);

        db.collection("results")
                .add(result)
                .addOnSuccessListener(doc -> showResultDialog())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save result: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showResultDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quiz Completed 🎉")
                .setMessage("You scored " + score + " out of " + questionList.size())
                .setPositiveButton("OK", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }
}
