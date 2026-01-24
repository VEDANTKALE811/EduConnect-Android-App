package com.example.educonnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class StudentMaterialAdapter extends RecyclerView.Adapter<StudentMaterialAdapter.ViewHolder> {

    private Context context;
    private List<DocumentSnapshot> materials;

    public StudentMaterialAdapter(Context context, List<DocumentSnapshot> materials) {
        this.context = context;
        this.materials = materials;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_material, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = materials.get(position);

        holder.tvTitle.setText("📘 " + doc.getString("title"));
        holder.tvDesc.setText(doc.getString("description"));

        String link = doc.getString("driveLink");

        holder.btnOpen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            context.startActivity(intent);
        });

        // ❌ Hide delete button for students
        holder.btnDelete.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;
        MaterialButton btnOpen, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btnOpen = itemView.findViewById(R.id.btnOpen);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
