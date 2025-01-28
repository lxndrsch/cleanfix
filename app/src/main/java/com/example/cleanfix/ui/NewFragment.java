package com.example.cleanfix.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.adapter.PhotoAdapter;
import com.example.cleanfix.model.Issue;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewFragment extends Fragment {

    private static final String TAG = "NewFragment";
    private static final int PERMISSION_CODE = 1000;

    private TextView locationTextView;
    private EditText descriptionEditText;
    private RecyclerView photoRecyclerView;
    private Button takePhotoButton, submitPhotosButton;

    private String currentPhotoPath;
    private ArrayList<Uri> photoUris = new ArrayList<>();
    private PhotoAdapter photoAdapter;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private String country;
    private String postalCode;
    private String timezone;
    private String addressText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView dateTextView = view.findViewById(R.id.date_text_view);
        locationTextView = view.findViewById(R.id.location_text_view);
        descriptionEditText = view.findViewById(R.id.description_edit_text);
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        takePhotoButton = view.findViewById(R.id.take_photo_button);
        submitPhotosButton = view.findViewById(R.id.submit_photos_button);

        // Dynamically set today's date
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        dateTextView.setText(String.format(Locale.getDefault(), "Date: %s", currentDate.toString()));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        setActivityLauncher();

        photoAdapter = new PhotoAdapter(photoUris, 4,true);
        photoRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        photoRecyclerView.setAdapter(photoAdapter);

        takePhotoButton.setOnClickListener(v -> checkPermissionsAndTakePhoto());
        submitPhotosButton.setOnClickListener(v -> uploadIssueToFirebase());
    }

    private void setActivityLauncher() {
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
                        photoUris.add(photoUri);
                        photoAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Photo successfully taken: " + photoUri);
                    } else {
                        Log.d(TAG, "Photo taking cancelled or failed.");
                    }
                }
        );
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile;

        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error creating file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(requireContext(), "com.example.cleanfix.fileprovider", photoFile);
            currentPhotoPath = photoFile.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            someActivityResultLauncher.launch(takePictureIntent);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String locationText = String.format(Locale.getDefault(), "Lat: %.5f, Lon: %.5f\n", latitude, longitude);
                locationTextView.setText(locationText + "Fetching address...");
                fetchAddress(latitude, longitude);
            } else {
                locationTextView.setText("Location: Not Available");
            }
        });
    }

    private void fetchAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressText = address.getAddressLine(0);
                    country = address.getCountryName();
                    postalCode = address.getPostalCode();

                    // Get timezone using the latitude and longitude
                    timezone = java.util.TimeZone.getDefault().getID();

                    requireActivity().runOnUiThread(() -> {
                        String locationText = String.format(Locale.getDefault(), "Address: %s", addressText);
                        locationTextView.setText(locationText);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> locationTextView.setText("Address: Not Available"));
                }
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> locationTextView.setText("Address: Not Available"));
            }
        }).start();
    }

    private void checkPermissionsAndTakePhoto() {
        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (cameraPermissionGranted && locationPermissionGranted) {
            dispatchTakePictureIntent();
            fetchLocation();
        } else {
            ArrayList<String> permissionsToRequest = new ArrayList<>();
            if (!cameraPermissionGranted) permissionsToRequest.add(Manifest.permission.CAMERA);
            if (!locationPermissionGranted) permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);

            requestPermissions(permissionsToRequest.toArray(new String[0]), PERMISSION_CODE);
        }
    }

    private void uploadIssueToFirebase() {
        String description = descriptionEditText.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide a description.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoUris.isEmpty()) {
            Toast.makeText(requireContext(), "No photos to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentLocation == null) {
            Toast.makeText(requireContext(), "No location data available", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("issues");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        List<String> photoUrls = new ArrayList<>();
        for (Uri photoUri : photoUris) {
            String fileName = "issues/" + System.currentTimeMillis() + "_" + new File(photoUri.getPath()).getName();
            StorageReference photoRef = storageReference.child(fileName);

            photoRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        photoUrls.add(uri.toString());

                        // Check if all photos are uploaded
                        if (photoUrls.size() == photoUris.size()) {
                            String issueId = databaseReference.push().getKey();
                            if (issueId != null) {
                                Issue issue = new Issue(
                                        issueId, userId, description,
                                        currentLocation.getLatitude(), currentLocation.getLongitude(), addressText,
                                        photoUrls, "new",
                                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()),
                                        timezone, country, postalCode
                                );

                                databaseReference.child(issueId).setValue(issue)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(requireContext(), "Issue uploaded successfully.", Toast.LENGTH_SHORT).show();
                                            resetForm();

                                            // Redirect to StatusFragment
                                            requireActivity().getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.container, new StatusFragment())
                                                    .commit();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload issue.", Toast.LENGTH_SHORT).show());
                            }
                        }
                    }))
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload photo.", Toast.LENGTH_SHORT).show());
        }
    }

    private void resetForm() {
        descriptionEditText.setText("");
        photoUris.clear();
        photoAdapter.notifyDataSetChanged();
        locationTextView.setText("Location: Not Available");
        currentLocation = null;
    }
}
