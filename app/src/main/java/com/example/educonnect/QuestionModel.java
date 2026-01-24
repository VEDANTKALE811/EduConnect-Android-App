package com.example.educonnect;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class QuestionModel {
    private String question;
    private List<String> options;
    private String answer;

    public QuestionModel(String question, JSONArray optionsArray, String answer) {
        this.question = question;
        this.options = new ArrayList<>();
        for (int i = 0; i < optionsArray.length(); i++) {
            this.options.add(optionsArray.optString(i));
        }
        this.answer = answer;
    }

    public QuestionModel(String question, List<String> options, String answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
    }

    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public String getAnswer() { return answer; }
}
