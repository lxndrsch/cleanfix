package com.example.cleanfix.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.adapter.StatusAdapter;
import com.example.cleanfix.model.Issue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";

    private RecyclerView statusRecyclerView;
    private ProgressBar progressBar;
    private TextView noIssuesTextView;

    private StatusAdapter statusAdapter;
    private List<Issue> issueList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statusRecyclerView = view.findViewById(R.id.status_recycler_view);
        progressBar = view.findViewById(R.id.status_progress_bar);
        noIssuesTextView = view.findViewById(R.id.no_issues_text_view);

        setupRecyclerView();
        fetchIssuesFromFirebase();
    }

    private void setupRecyclerView() {
        statusAdapter = new StatusAdapter(issueList);
        statusRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        statusRecyclerView.setAdapter(statusAdapter);
    }

    private void fetchIssuesFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("issues");

        progressBar.setVisibility(View.VISIBLE);
        noIssuesTextView.setVisibility(View.GONE);

        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                issueList.clear();

                for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                    Issue issue = issueSnapshot.getValue(Issue.class);
                    if (issue != null) {
                        issueList.add(issue);
                    }
                }

                progressBar.setVisibility(View.GONE);
                if (issueList.isEmpty()) {
                    noIssuesTextView.setVisibility(View.VISIBLE);
                } else {
                    noIssuesTextView.setVisibility(View.GONE);
                }
                statusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                noIssuesTextView.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Failed to fetch issues.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }
}
