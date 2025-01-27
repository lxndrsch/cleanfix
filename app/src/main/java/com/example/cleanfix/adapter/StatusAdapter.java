package com.example.cleanfix.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.model.Issue;
import com.example.cleanfix.ui.IssueDetailActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
                intent.putExtra("address", issue.getaddressText());
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

        // Set up PhotoAdapter for the issue's photos
        if (issue.getPhotoUrls() != null && !issue.getPhotoUrls().isEmpty()) {
            ArrayList<Uri> photoUris = new ArrayList<>();
            for (String url : issue.getPhotoUrls()) {
                photoUris.add(Uri.parse(url)); // Convert String to Uri
            }

            PhotoAdapter photoAdapter = new PhotoAdapter(photoUris,1); // Pass the converted list of Uris
            holder.photosRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.photosRecyclerView.setAdapter(photoAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder {
        private final TextView descriptionTextView;
        private final RecyclerView photosRecyclerView;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            photosRecyclerView = itemView.findViewById(R.id.photos_recycler_view);
        }

        public void bind(Issue issue) {
            // Define the input format (ISO 8601)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Important for handling 'Z' as UTC

            // Define the output format
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            String formattedDate = "";
            try {
                // Parse the ISO 8601 timestamp
                Date date = inputFormat.parse(issue.getTimestamp());

                // Format the date into the desired format
                formattedDate = outputFormat.format(date);
            } catch (ParseException e) {
                formattedDate = issue.getTimestamp();  // Fallback to raw timestamp if parsing fails
            }

            // Concatenate the formatted date, address, and status
            String description = formattedDate + "\n" +
                    issue.getaddressText() + "\n" +
                    "Status: " + issue.getStatus();

            // Set the description to the TextView
            descriptionTextView.setText(description);
        }
    }
}
