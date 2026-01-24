package com.example.educonnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QuestionPaperAdapter extends FirestoreRecyclerAdapter<QuestionPaperModel, QuestionPaperAdapter.PaperViewHolder> {

    public QuestionPaperAdapter(@NonNull FirestoreRecyclerOptions<QuestionPaperModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PaperViewHolder holder, int position, @NonNull QuestionPaperModel model) {
        holder.tvTitle.setText(model.getTitle());
        String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(model.getTimestamp()));
        holder.tvDate.setText("Uploaded on " + date);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(model.getUrl()), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(intent);
        });
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_paper, parent, false);
        return new PaperViewHolder(v);
    }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPaperTitle);
            tvDate = itemView.findViewById(R.id.tvPaperDate);
        }
    }
}
