package com.example.educonnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private final List<DocumentSnapshot> resultList;

    public ResultAdapter(List<DocumentSnapshot> resultList) {
        this.resultList = resultList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = resultList.get(position);

        String quizTitle = doc.getString("quizTitle");
        long score = doc.getLong("score") != null ? doc.getLong("score") : 0;
        long total = doc.getLong("total") != null ? doc.getLong("total") : 0;
        long timestamp = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0;

        String formattedDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(timestamp));

        holder.tvQuizTitle.setText(quizTitle != null ? quizTitle : "Unknown Quiz");
        holder.tvScore.setText("Score: " + score + "/" + total);
        holder.tvDate.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizTitle, tvScore, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvQuizTitle = itemView.findViewById(R.id.tvQuizTitle);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
