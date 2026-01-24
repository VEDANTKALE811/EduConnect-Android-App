package com.example.educonnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private final Context context;
    private final List<MemberModel> members;

    public MemberAdapter(Context context, List<MemberModel> members) {
        this.context = context;
        this.members = members;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        MemberModel m = members.get(pos);
        h.tvName.setText(m.getName() != null ? m.getName() : "Unknown User");
        h.tvEmail.setText(m.getEmail() != null ? m.getEmail() : "No email");

        if (m.getProfileImage() != null && !m.getProfileImage().isEmpty()) {
            Glide.with(context)
                    .load(m.getProfileImage())
                    .placeholder(R.drawable.ic_person)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .circleCrop()
                    .into(h.imgAvatar);
        } else {
            h.imgAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvEmail;

        ViewHolder(View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            tvName = v.findViewById(R.id.tvName);
            tvEmail = v.findViewById(R.id.tvEmail);
        }
    }
}
