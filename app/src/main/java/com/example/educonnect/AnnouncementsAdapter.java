package com.example.educonnect;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder> {

    private final Context context;
    private final List<Announcement> list;
    private final boolean isAdmin;

    public AnnouncementsAdapter(Context context, List<Announcement> list, boolean isAdmin) {
        this.context = context;
        this.list = list;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Announcement ann = list.get(position);
        holder.tvTitle.setText(ann.getTitle());
        holder.tvDescription.setText(ann.getDescription());

        String time = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                .format(ann.getTimestamp());
        holder.tvTimestamp.setText("Posted on " + time);

        // 🗑️ Long press delete for admins
        if (isAdmin) {
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Announcement?")
                        .setMessage("Are you sure you want to remove this announcement?")
                        .setPositiveButton("Delete", (d, w) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("announcements")
                                    .whereEqualTo("timestamp", ann.getTimestamp())
                                    .get()
                                    .addOnSuccessListener(snapshot -> {
                                        snapshot.getDocuments().forEach(doc -> doc.getReference().delete());
                                        Toast.makeText(context, "Deleted successfully ✅", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTimestamp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
