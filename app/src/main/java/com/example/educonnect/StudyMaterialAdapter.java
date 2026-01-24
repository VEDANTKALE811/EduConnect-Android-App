package com.example.educonnect;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;

public class StudyMaterialAdapter extends RecyclerView.Adapter<StudyMaterialAdapter.ViewHolder> {

    private final List<StudyMaterialModel> list;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String url);
    }

    public StudyMaterialAdapter(List<StudyMaterialModel> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_material, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyMaterialModel material = list.get(position);
        holder.tvTitle.setText(material.getTitle());
        holder.tvUploader.setText("Uploaded by: " + material.getUploadedBy());
        holder.tvDate.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(material.getTimestamp())));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(material.getUrl()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvUploader, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMaterialTitle);
            tvUploader = itemView.findViewById(R.id.tvMaterialUploader);
            tvDate = itemView.findViewById(R.id.tvMaterialDate);
        }
    }
}
