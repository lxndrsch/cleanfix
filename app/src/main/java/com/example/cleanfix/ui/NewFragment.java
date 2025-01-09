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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
