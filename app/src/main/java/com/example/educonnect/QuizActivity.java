package com.example.educonnect;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {

    private EditText etSearch;
    private MaterialButton btnGenerate;
    private RecyclerView rvQuestions, rvOldQuizzes;

    private QuestionAdapter adapter;
    private QuizListAdapter quizListAdapter;

    private final ArrayList<QuestionModel> questionList = new ArrayList<>();
    private final List<DocumentSnapshot> oldQuizList = new ArrayList<>();

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    // ⚠️ Replace with your valid Gemini API key from Google AI Studio
    private static final String API_KEY = "AIzaSyBp9LYdXkjUp9E30XaLQr2JKnnQFDhO8Uw";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-001:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearch);
        btnGenerate = findViewById(R.id.btnGenerate);
        rvQuestions = findViewById(R.id.rvQuestions);
        rvOldQuizzes = findViewById(R.id.rvOldQuizzes);

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvOldQuizzes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new QuestionAdapter(questionList);
        rvQuestions.setAdapter(adapter);

        quizListAdapter = new QuizListAdapter(
                oldQuizList,
                quizId -> Toast.makeText(this, "Selected Quiz ID: " + quizId, Toast.LENGTH_SHORT).show(),
                this::deleteQuiz
        );

        rvOldQuizzes.setAdapter(quizListAdapter);

        btnGenerate.setOnClickListener(v -> {
            String topic = etSearch.getText().toString().trim();
            if (topic.isEmpty()) {
                Toast.makeText(this, "Enter a topic first", Toast.LENGTH_SHORT).show();
                return;
            }
            generateAIQuestions(topic, 5);
        });

        loadOldQuizzes();
    }

    // ✅ Load quizzes from Firestore
    private void loadOldQuizzes() {
        firestore.collection("quizzes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    oldQuizList.clear();
                    oldQuizList.addAll(snapshots.getDocuments());
                    quizListAdapter.notifyDataSetChanged();

                    if (oldQuizList.isEmpty()) {
                        Toast.makeText(this, "No old quizzes available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading quizzes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ✅ Delete quiz from Firestore
    private void deleteQuiz(String quizId) {
        firestore.collection("quizzes").document(quizId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Quiz deleted successfully", Toast.LENGTH_SHORT).show();
                    loadOldQuizzes();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ✅ Generate quiz using Gemini API
    private void generateAIQuestions(String topic, int numQuestions) {
        btnGenerate.setEnabled(false);
        questionList.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Generating " + numQuestions + " questions on \"" + topic + "\" ...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();

        String prompt = "Generate " + numQuestions + " multiple-choice quiz questions on " + topic +
                ". Return a valid JSON array only in this format: " +
                "[{\"question\":\"What is...?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":\"A\"}]";

        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(part));
            jsonBody.put("contents", new JSONArray().put(content));
        } catch (Exception e) {
            Log.e("QUIZ_JSON", "Error building prompt JSON: " + e.getMessage());
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    btnGenerate.setEnabled(true);
                    Toast.makeText(QuizActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> btnGenerate.setEnabled(true));

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(QuizActivity.this, "API request failed: " + response.message(), Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String text = json.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    // 🔹 Clean up and extract only JSON array content
                    text = text.replaceAll("```json", "")
                            .replaceAll("```", "")
                            .replaceAll("\n", "")
                            .trim();

                    if (!text.startsWith("[")) {
                        int start = text.indexOf("[");
                        int end = text.lastIndexOf("]");
                        if (start >= 0 && end >= 0 && end > start)
                            text = text.substring(start, end + 1);
                    }

                    JSONArray questions = new JSONArray(text);
                    questionList.clear();

                    for (int i = 0; i < questions.length(); i++) {
                        JSONObject q = questions.getJSONObject(i);
                        questionList.add(new QuestionModel(
                                q.getString("question"),
                                q.getJSONArray("options"),
                                q.getString("answer")
                        ));
                    }

                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        saveQuizToFirestore(topic, questionList);
                        loadOldQuizzes();
                    });

                } catch (Exception e) {
                    Log.e("QUIZ_PARSE", "Parsing error: " + e.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(QuizActivity.this, "Error parsing AI response: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    // ✅ Save generated quiz to Firestore
    private void saveQuizToFirestore(String topic, ArrayList<QuestionModel> questionList) {
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("title", topic);
        quizData.put("createdBy", auth.getUid());
        quizData.put("createdAt", System.currentTimeMillis());

        List<Map<String, Object>> questions = new ArrayList<>();
        for (QuestionModel q : questionList) {
            Map<String, Object> map = new HashMap<>();
            map.put("question", q.getQuestion());
            map.put("options", q.getOptions());
            map.put("answer", q.getAnswer());
            questions.add(map);
        }
        quizData.put("questions", questions);

        firestore.collection("quizzes").add(quizData)
                .addOnSuccessListener(doc -> {
                    quizData.put("id", doc.getId());
                    firestore.collection("quizzes").document(doc.getId()).update(quizData);
                    Toast.makeText(this, "✅ Quiz saved to Firestore!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Failed to save quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
