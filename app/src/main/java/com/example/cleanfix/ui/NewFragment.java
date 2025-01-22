package com.example.cleanfix.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.adapter.PhotoAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewFragment extends Fragment {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private String currentPhotoPath;
    private ArrayList<Uri> photoUris = new ArrayList<>();
    private PhotoAdapter photoAdapter;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private static final String TAG = "NewFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActivityLauncher();

        RecyclerView photoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        photoAdapter = new PhotoAdapter(photoUris);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        photoRecyclerView.setAdapter(photoAdapter);

        Button takePhotoButton = view.findViewById(R.id.take_photo_button);
        Button submitPhotosButton = view.findViewById(R.id.submit_photos_button);

        takePhotoButton.setOnClickListener(v -> dispatchTakePictureIntent());
        submitPhotosButton.setOnClickListener(v -> uploadPhotosToFirebase());
    }

    private void setActivityLauncher() {
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Add the photo's URI to the list
                            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
                            photoUris.add(photoUri);
                            photoAdapter.notifyDataSetChanged();

                            Log.d(TAG, "Photo successfully taken and added to list: " + photoUri.toString());
                        } else {
                            Log.d(TAG, "Photo taking cancelled or failed.");
                        }
                    }
                }
        );
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;

        try {
            Log.d(TAG, "Creating image file...");
            photoFile = createImageFile();
            Log.d(TAG, "Image file created: " + photoFile.getAbsolutePath());
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error creating file", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error creating file", ex);
            return;
        }

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(requireContext(), "com.example.cleanfix.fileprovider", photoFile);
            currentPhotoPath = photoFile.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            someActivityResultLauncher.launch(takePictureIntent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Photo successfully taken. Path: " + currentPhotoPath);
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            photoUris.add(photoUri);
            photoAdapter.notifyDataSetChanged();
            Log.d(TAG, "Photo URI added: " + photoUri.toString());
        } else {
            Log.d(TAG, "Photo not taken or operation failed.");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadPhotosToFirebase() {
        if (photoUris.isEmpty()) {
            Toast.makeText(requireContext(), "No photos to upload", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No photos to upload.");
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        for (Uri photoUri : photoUris) {
            Log.d(TAG, "Uploading photo: " + photoUri.toString());
            String fileName = "issues/" + System.currentTimeMillis() + "_" + new File(photoUri.getPath()).getName();
            StorageReference photoRef = storageReference.child(fileName);

            photoRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(requireContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Uploaded: " + fileName);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Upload failed", e);
                    });
        }
    }
}
