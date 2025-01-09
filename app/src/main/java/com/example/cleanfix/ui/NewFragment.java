package com.example.cleanfix.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.cleanfix.R;
import com.example.cleanfix.databinding.FragmentNewBinding;

import java.io.File;

public class NewFragment extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 1002;
    private static final String IMAGE_FILE_NAME = "test.png";
    private static final String TAG = "NewFragment";
    private File photoFile = null;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private FragmentNewBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "Initialize imageView Listener");

        binding = FragmentNewBinding.inflate(inflater, container, false);
        setActivityLauncher();
        binding.imageView.setOnClickListener(v -> takePhoto());

        return binding.getRoot();
    }

    private void setActivityLauncher() {
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(TAG, "Photo has been taken");
                        } else {
                            Log.e(TAG, "Failed to capture photo.");
                        }
                    }
                });
    }

    private void takePhoto() {
        Log.i(TAG, "Use system camera to take photo");

        // Check for CAMERA and WRITE_EXTERNAL_STORAGE permissions
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_REQUEST_CODE);
            return;
        }

        // use Intent to access camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = createPhotoFile();
        if (imageFile == null) {
            Log.e(TAG, "Failed to create photo file.");
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(requireActivity(), "com.example.cleanfix.ui.fileprovider", imageFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            someActivityResultLauncher.launch(takePictureIntent);
        } else {
            Log.e(TAG, "No camera app available to handle the intent.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private File createPhotoFile() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.d(TAG, "External storage not mounted.");
            return null;
        }

        File fileDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (fileDir != null && !fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                Log.d(TAG, "Failed to create directory.");
                return null;
            }
        }

        return new File(fileDir, IMAGE_FILE_NAME);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Log.e(TAG, "Camera permission denied.");
            }
        } else if (requestCode == WRITE_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Log.e(TAG, "Write storage permission denied.");
            }
        }
    }

}
