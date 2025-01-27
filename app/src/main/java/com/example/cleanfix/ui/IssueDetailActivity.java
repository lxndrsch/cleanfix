package com.example.cleanfix.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cleanfix.R;
import com.example.cleanfix.model.Issue;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class IssueDetailActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button markResolvedButton;

    private String issueId;
    private Issue issue;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        backButton = findViewById(R.id.back_button);
        markResolvedButton = findViewById(R.id.resolve_button);

        issueId = getIntent().getStringExtra("issueId");
        issue = (Issue) getIntent().getSerializableExtra("issue");  // Or get the Issue object

        databaseReference = FirebaseDatabase.getInstance().getReference("issues");

        backButton.setOnClickListener(v -> {
            // Navigate back to StatusFragment
            onBackPressed();
        });

        markResolvedButton.setOnClickListener(v -> {
            // Change the issue status to "Resolved"
            if (issue != null) {
                issue.setStatus("Resolved");
                databaseReference.child(issueId).setValue(issue)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Issue marked as resolved", Toast.LENGTH_SHORT).show();
                            // Navigate back to StatusFragment
                            onBackPressed();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        });

            }
            onBackPressed();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // You can use `finish()` to close the current activity and return to StatusFragment
        finish();
    }
}
