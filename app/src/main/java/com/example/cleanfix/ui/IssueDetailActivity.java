package com.example.cleanfix.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.adapter.PhotoAdapter;

import java.util.ArrayList;
import java.util.List;

public class IssueDetailActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView descriptionTextView, statusTextView;
    private RecyclerView photosRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        descriptionTextView = findViewById(R.id.description_text_view);
        statusTextView = findViewById(R.id.status_text_view);
        photosRecyclerView = findViewById(R.id.photos_recycler_view);

        // Get data from Intent
        String description = getIntent().getStringExtra("description");
        String status = getIntent().getStringExtra("status");
        ArrayList<String> photoUrls = getIntent().getStringArrayListExtra("photoUrls");

        // Set issue details
        descriptionTextView.setText(description);
        statusTextView.setText(String.format("Status: %s", status));

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
    }

    private void setupPhotoRecyclerView(List<Uri> photoUris) {
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        PhotoAdapter photoAdapter = new PhotoAdapter(photoUris, 4);
        photosRecyclerView.setAdapter(photoAdapter);
    }
}
