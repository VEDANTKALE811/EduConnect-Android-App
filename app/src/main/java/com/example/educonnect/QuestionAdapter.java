package com.example.educonnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private final List<QuestionModel> questionList;

    public QuestionAdapter(List<QuestionModel> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionModel q = questionList.get(position);
        holder.tvQuestion.setText((position + 1) + ". " + q.getQuestion());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < q.getOptions().size(); i++) {
            sb.append((char) ('A' + i))
                    .append(". ")
                    .append(q.getOptions().get(i))
                    .append("\n");
        }

        holder.tvOptions.setText(sb.toString().trim());
        holder.tvAnswer.setText("Answer: " + q.getAnswer());
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvOptions, tvAnswer;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvOptions = itemView.findViewById(R.id.tvOptions);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
        }
    }
}
