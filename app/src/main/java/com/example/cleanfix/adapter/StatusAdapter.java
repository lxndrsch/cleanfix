package com.example.cleanfix.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cleanfix.R;
import com.example.cleanfix.model.Issue;
import com.example.cleanfix.ui.IssueDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private final List<Issue> issueList;
    private final Context context;

    public StatusAdapter(List<Issue> issueList, Context context) {
        this.issueList = issueList;
        this.context = context;
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

        if (issue == null) {
            Log.e("StatusAdapter", "Issue at position " + position + " is null");
            return;
        }

        holder.bind(issue);

        // Open details activity on click
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, IssueDetailActivity.class);
                intent.putExtra("issueId", issue.getIssueId());
                intent.putExtra("userId", issue.getUserId());
                intent.putExtra("description", issue.getDescription());
                intent.putExtra("latitude", issue.getLatitude());
                intent.putExtra("longitude", issue.getLongitude());
                intent.putExtra("status", issue.getStatus());
                intent.putExtra("timestamp", issue.getTimestamp());

                // Handle photoUrls safely
                if (issue.getPhotoUrls() != null && !issue.getPhotoUrls().isEmpty()) {
                    intent.putStringArrayListExtra("photoUrls", new ArrayList<>(issue.getPhotoUrls()));
                }

                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Error opening details", Toast.LENGTH_SHORT).show();
                Log.e("StatusAdapter", "Error starting IssueDetailActivity: ", e);
            }
        });
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

            if (issue.getPhotoUrls() != null && !issue.getPhotoUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(issue.getPhotoUrls().get(0))
                        .placeholder(R.drawable.placeholder_image)
                        .into(issueImageView);
            } else {
                issueImageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
