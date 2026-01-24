package com.example.educonnect;

import android.content.Context;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    public interface OnGroupClickListener {
        void onViewMembers(GroupModel group);
        void onDeleteGroup(GroupModel group);
    }

    private final Context context;
    private final List<GroupModel> list;
    private final OnGroupClickListener listener;
    private final String currentUserId;

    public GroupAdapter(Context context, List<GroupModel> list, OnGroupClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        GroupModel g = list.get(pos);
        h.tvName.setText(g.getGroupName());
        h.tvCode.setText("Code: " + g.getGroupCode());
        h.tvDesc.setText(g.getDescription());

        // Show delete icon only for admin
        if (g.getCreatedBy() != null && g.getCreatedBy().equals(currentUserId)) {
            h.btnDelete.setVisibility(View.VISIBLE);
        } else {
            h.btnDelete.setVisibility(View.GONE);
        }

        h.card.setOnClickListener(v -> listener.onViewMembers(g));
        h.btnDelete.setOnClickListener(v -> listener.onDeleteGroup(g));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvDesc;
        ImageButton btnDelete;
        MaterialCardView card;

        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvGroupName);
            tvCode = v.findViewById(R.id.tvGroupCode);
            tvDesc = v.findViewById(R.id.tvGroupDesc);
            btnDelete = v.findViewById(R.id.btnDelete);
            card = v.findViewById(R.id.cardGroup);
        }
    }
}
