package com.example.educonnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

/**
 * Adapter for displaying quizzes (used in both ViewQuizActivity and QuizActivity).
 * Supports click and delete actions.
 */
public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.ViewHolder> {

    public interface OnQuizClickListener {
        void onQuizClick(String quizId);
    }

    public interface OnQuizDeleteListener {
        void onQuizDelete(String quizId);
    }

    private final List<DocumentSnapshot> quizList;
    private final OnQuizClickListener clickListener;
    private final OnQuizDeleteListener deleteListener;
    private final boolean showDelete; // For admin visibility

    // ✅ Constructor (for both Admin and Student)
    public QuizListAdapter(List<DocumentSnapshot> quizList,
                           OnQuizClickListener clickListener,
                           OnQuizDeleteListener deleteListener) {
        this.quizList = quizList;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
        this.showDelete = (deleteListener != null); // show delete only if admin
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_admin_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot quiz = quizList.get(position);

        String title = quiz.getString("title");
        long createdAt = quiz.contains("createdAt") ? quiz.getLong("createdAt") : 0;

        holder.tvTitle.setText(title != null ? title : "Untitled Quiz");

        // Hide delete button for students
        holder.btnDelete.setVisibility(showDelete ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onQuizClick(quiz.getId());
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onQuizDelete(quiz.getId());
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuizTitle);
            btnDelete = itemView.findViewById(R.id.btnDeleteQuiz);
        }
    }
}
