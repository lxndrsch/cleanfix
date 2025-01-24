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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.adapter.PhotoAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
    private EditText descriptionEditText; // New field for description
    private RecyclerView photoRecyclerView;
    private Button takePhotoButton, submitPhotosButton;

    private String currentPhotoPath;
    private ArrayList<Uri> photoUris = new ArrayList<>();
    private PhotoAdapter photoAdapter;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationTextView = view.findViewById(R.id.location_text_view);
        descriptionEditText = view.findViewById(R.id.description_edit_text);
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        takePhotoButton = view.findViewById(R.id.take_photo_button);
        submitPhotosButton = view.findViewById(R.id.submit_photos_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        setActivityLauncher();

        photoAdapter = new PhotoAdapter(photoUris);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                Log.d(TAG, "Location captured.");
            } else {
                locationTextView.setText("Location: Not Available");
                Log.d(TAG, "Failed to get location.");
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
                    String addressText = address.getAddressLine(0);

                    requireActivity().runOnUiThread(() -> {
                        String locationText = String.format(Locale.getDefault(), "Address: %s", addressText);
                        locationTextView.setText(locationText);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> locationTextView.setText("Address: Not Available"));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error fetching address: ", e);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            boolean cameraPermissionGranted = false;
            boolean locationPermissionGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    cameraPermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    locationPermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }

            if (cameraPermissionGranted && locationPermissionGranted) {
                dispatchTakePictureIntent();
                fetchLocation();
            } else {
                Toast.makeText(requireContext(), "Permissions are required to use this feature.", Toast.LENGTH_SHORT).show();
            }
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

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("issues");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        for (Uri photoUri : photoUris) {
            String fileName = "issues/" + System.currentTimeMillis() + "_" + new File(photoUri.getPath()).getName();
            StorageReference photoRef = storageReference.child(fileName);

            photoRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(requireContext(), "Photo uploaded", Toast.LENGTH_SHORT).show();

                        String issueId = databaseReference.push().getKey();
                        if (issueId != null) {
                            databaseReference.child(issueId).setValue(new Issue(
                                    currentLocation.getLatitude(),
                                    currentLocation.getLongitude(),
                                    fileName,
                                    description
                            ));
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Photo upload failed", e));
        }
    }

    static class Issue {
        public double latitude;
        public double longitude;
        public String photoPath;
        public String description;

        public Issue(double latitude, double longitude, String photoPath, String description) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.photoPath = photoPath;
            this.description = description;
        }
    }
}
