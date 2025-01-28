package com.example.cleanfix.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.adapter.StatusAdapter;
import com.example.cleanfix.model.Issue;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
    private static final float MAX_DISTANCE_KM = 1.0f; // 1 km
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private RecyclerView statusRecyclerView;
    private ProgressBar progressBar;
    private TextView noIssuesTextView;
    private Spinner filterSpinner;
    private Button filterWithin1KmButton;

    private StatusAdapter statusAdapter;
    private List<Issue> issueList = new ArrayList<>();
    private String selectedFilter = "New"; // Default filter value
    private boolean isFilterWithin1KmEnabled = true; // Button enabled by default

    private FusedLocationProviderClient fusedLocationClient;

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
        filterSpinner = view.findViewById(R.id.filter_spinner);
        filterWithin1KmButton = view.findViewById(R.id.filter_within_1km_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        setupRecyclerView();
        setupFilterSpinner();
        setupFilterButton();
        checkAndRequestLocationPermission();
    }

    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission if not granted
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed with fetching issues
            fetchIssuesFromFirebase();
        }
    }

    private void setupRecyclerView() {
        statusAdapter = new StatusAdapter(issueList, requireContext());
        statusRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        statusRecyclerView.setAdapter(statusAdapter);
    }

    private void setupFilterSpinner() {
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = parent.getItemAtPosition(position).toString(); // Get the selected filter
                fetchIssuesFromFirebase(); // Fetch issues based on the selected filter
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default filter remains "New"
            }
        });
    }

    private void setupFilterButton() {
        filterWithin1KmButton.setOnClickListener(v -> {
            isFilterWithin1KmEnabled = !isFilterWithin1KmEnabled; // Toggle button state
            filterWithin1KmButton.setText(isFilterWithin1KmEnabled ? "Within 1 km" : "Show all issues");
            fetchIssuesFromFirebase(); // Re-fetch issues with the updated filter
        });
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

        // Fetch issues based on userId and selected filter (status)
        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                issueList.clear();

                // If filter is enabled, get user's current location
                if (isFilterWithin1KmEnabled) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(requireActivity(), location -> {
                                if (location != null) {
                                    filterIssuesByDistance(location, snapshot);
                                }
                            });
                } else {
                    // No distance filter, just apply the status filter
                    for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
                        Issue issue = issueSnapshot.getValue(Issue.class);
                        if (issue != null && issue.getStatus().equalsIgnoreCase(selectedFilter)) {
                            issueList.add(issue);
                        }
                    }
                    displayIssues();
                }
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

    private void filterIssuesByDistance(Location userLocation, DataSnapshot snapshot) {
        for (DataSnapshot issueSnapshot : snapshot.getChildren()) {
            Issue issue = issueSnapshot.getValue(Issue.class);
            if (issue != null && issue.getStatus().equalsIgnoreCase(selectedFilter)) {
                // Get issue location and calculate the distance
                float[] results = new float[1];
                Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                        issue.getLatitude(), issue.getLongitude(), results);

                if (results[0] <= MAX_DISTANCE_KM * 1000) { // Within 1 km
                    issueList.add(issue);
                }
            }
        }
        displayIssues();
    }

    private void displayIssues() {
        progressBar.setVisibility(View.GONE);
        if (issueList.isEmpty()) {
            noIssuesTextView.setVisibility(View.VISIBLE);
        } else {
            noIssuesTextView.setVisibility(View.GONE);
        }
        statusAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with fetching issues
                fetchIssuesFromFirebase();
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
