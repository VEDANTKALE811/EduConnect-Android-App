package com.example.educonnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class MaterialItemAdapter extends RecyclerView.Adapter<MaterialItemAdapter.ViewHolder> {

    private Context context;
    private List<DocumentSnapshot> materials;
    private String type;
    private FirebaseFirestore firestore;

    public MaterialItemAdapter(Context context, List<DocumentSnapshot> materials, String type) {
        this.context = context;
        this.materials = materials;
        this.type = type;
        this.firestore = FirebaseFirestore.getInstance();
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
        String id = doc.getId();

        holder.tvTitle.setText("📘 " + doc.getString("title"));
        holder.tvDesc.setText(doc.getString("description"));

        String link = doc.getString("driveLink");

        holder.btnOpen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            firestore.collection(type)
                    .document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                        materials.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
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
