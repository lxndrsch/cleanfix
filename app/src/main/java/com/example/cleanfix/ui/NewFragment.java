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
    private void setActivityLauncher() { someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) { if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG,"Photo has been taken"); }
                } });
    }
    private void takePhoto() {
        Log.d(TAG,"Use system camera to take photo");
        // use Intent to access camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); try {
            someActivityResultLauncher.launch(takePictureIntent); }
        catch (Exception e) {
            e.printStackTrace(); }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        someActivityResultLauncher.launch(takePictureIntent);
        File photoFile;
        try {
            Log.d(TAG,"create ImageFile");
            photoFile = createImageFile();
            Log.d(TAG,"created ImageFile"+photoFile);
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error creating file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(requireContext(), "com.example.cleanfix.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == requireActivity().RESULT_OK) {
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            photoUris.add(photoUri);
            photoAdapter.notifyDataSetChanged();
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
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        for (Uri photoUri : photoUris) {
            String fileName = "issues/" + System.currentTimeMillis() + "_" + photoUri.getLastPathSegment();
            StorageReference photoRef = storageReference.child(fileName);

            photoRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(requireContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                        Log.d("NewFragment", "Uploaded: " + fileName);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("NewFragment", "Upload failed", e);
                    });
        }
    }
}
