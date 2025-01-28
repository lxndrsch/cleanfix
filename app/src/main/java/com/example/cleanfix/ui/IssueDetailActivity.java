package com.example.cleanfix.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.adapter.PhotoAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class IssueDetailActivity extends AppCompatActivity {

    private Button backButton;
    private Button resolveButton;
    private TextView descriptionTextView, statusTextView;
    private RecyclerView photosRecyclerView;

    private String issueId; // To track the current issue ID
    private DatabaseReference issuesRef; // Firebase reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        // Initialize Firebase
        issuesRef = FirebaseDatabase.getInstance().getReference("issues");

        // Initialize views
        backButton = findViewById(R.id.back_button);
        resolveButton = findViewById(R.id.resolve_button);
        descriptionTextView = findViewById(R.id.description_text_view);
        statusTextView = findViewById(R.id.status_text_view);
        photosRecyclerView = findViewById(R.id.photos_recycler_view);

        // Get data from Intent
        issueId = getIntent().getStringExtra("issueId");
        String description = getIntent().getStringExtra("description");
        String status = getIntent().getStringExtra("status");
        String address = getIntent().getStringExtra("addressText");
        String timestamp = getIntent().getStringExtra("timestamp");
        ArrayList<String> photoUrls = getIntent().getStringArrayListExtra("photoUrls");

        // Set issue details (Concatenate date, address, and status)
        String issueDetails = String.format("%s\n%s\nStatus: %s\nDesscription: %s", timestamp, address, status, description);
        descriptionTextView.setText(issueDetails);

        // Handle photos
        if (photoUrls != null && !photoUrls.isEmpty()) {
            List<Uri> photoUris = new ArrayList<>();
            for (String url : photoUrls) {
                photoUris.add(Uri.parse(url));
            }
            setupPhotoRecyclerView(photoUris);
        }

        // Back button functionality
        backButton.setOnClickListener(v -> onBackPressed());

        // Resolve button functionality
        resolveButton.setOnClickListener(v -> resolveIssue());
    }

    private void setupPhotoRecyclerView(List<Uri> photoUris) {
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        PhotoAdapter photoAdapter = new PhotoAdapter(photoUris, 4);
        photosRecyclerView.setAdapter(photoAdapter);
    }

    private void resolveIssue() {
        if (issueId != null) {
            // Update the status to "Resolved" in Firebase
            issuesRef.child(issueId).child("status").setValue("Resolved")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Issue resolved successfully.", Toast.LENGTH_SHORT).show();
                        finish(); // Return to StatusFragment
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to resolve the issue.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
