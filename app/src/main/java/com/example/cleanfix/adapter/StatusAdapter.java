package com.example.cleanfix.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cleanfix.R;
import com.example.cleanfix.model.Issue;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private final List<Issue> issueList;

    public StatusAdapter(List<Issue> issueList) {
        this.issueList = issueList;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        Issue issue = issueList.get(position);
        holder.bind(issue);
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder {
        private final TextView descriptionTextView;
        private final TextView statusTextView;
        private final ImageView issueImageView;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            statusTextView = itemView.findViewById(R.id.status_text_view);
            issueImageView = itemView.findViewById(R.id.issue_image_view);
        }

        public void bind(Issue issue) {
            descriptionTextView.setText(issue.getDescription());
            statusTextView.setText(String.format("Status: %s", issue.getStatus()));

            // Load the first image in the photoUrls list, if available
            if (issue.getPhotoUrls() != null && !issue.getPhotoUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(issue.getPhotoUrls().get(0))
                        .placeholder(R.drawable.placeholder_image) // Add a placeholder image in your drawable folder
                        .into(issueImageView);
            } else {
                issueImageView.setImageResource(R.drawable.placeholder_image); // Set default placeholder if no image exists
            }
        }
    }
}
