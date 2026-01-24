package com.example.educonnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIQueryActivity extends AppCompatActivity {

    private EditText etQuery;
    private MaterialButton btnAsk;
    private TextView tvResponse;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    // ✅ Gemini API key (works for both Admin & Student)
    private static final String API_KEY = "AIzaSyBp9LYdXkjUp9E30XaLQr2JKnnQFDhO8Uw";

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-001:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_query);

        // 🔹 Initialize Views
        MaterialToolbar toolbar = findViewById(R.id.toolbarAI);
        etQuery = findViewById(R.id.etQuery);
        btnAsk = findViewById(R.id.btnAsk);
        tvResponse = findViewById(R.id.tvResponse);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);

        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ask button click
        btnAsk.setOnClickListener(v -> {
            String query = etQuery.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter a question!", Toast.LENGTH_SHORT).show();
                return;
            }
            askGeminiAI(query);
        });
    }

    /**
     * 🔹 Send request to Gemini API
     */
    private void askGeminiAI(String userQuery) {
        progressBar.setVisibility(View.VISIBLE);
        tvResponse.setText("");
        btnAsk.setEnabled(false);

        OkHttpClient client = new OkHttpClient();

        String prompt = "You are EduConnect AI Assistant — a polite, knowledgeable guide for students and admins. "
                + "If asked about studies, explain simply. If asked about management or system features, guide with steps. "
                + "Keep tone friendly and short. Here is the user’s question: " + userQuery;

        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject part = new JSONObject();
            part.put("text", prompt);

            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(part));

            jsonBody.put("contents", new JSONArray().put(content));
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(GEMINI_URL)
                .post(body)
                .build();

        // 🌐 API call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnAsk.setEnabled(true);
                    Toast.makeText(AIQueryActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnAsk.setEnabled(true);
                });

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(AIQueryActivity.this, "❌ API request failed", Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseBody = response.body().string();
                Log.d("GeminiResponse", responseBody);

                try {
                    JSONObject json = new JSONObject(responseBody);
                    String text = json.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                            .trim();

                    runOnUiThread(() -> {
                        typeTextEffect(text);
                        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            tvResponse.setText("⚠️ Couldn’t parse AI response."));
                }
            }
        });
    }

    /**
     * 🪄 Smooth typing effect for AI responses
     */
    private void typeTextEffect(String fullText) {
        tvResponse.setText("");
        new Thread(() -> {
            for (int i = 0; i < fullText.length(); i++) {
                final int index = i;
                runOnUiThread(() -> tvResponse.append(String.valueOf(fullText.charAt(index))));
                try {
                    Thread.sleep(15); // adjust typing speed here
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
