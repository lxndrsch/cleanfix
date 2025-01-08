package com.example.cleanfix.ui;

import android.app.Activity;
import android.content.Intent;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.cleanfix.R;
import com.example.cleanfix.databinding.FragmentNewBinding;

import java.io.File;

public class NewFragment extends Fragment {
    private File photoFile = null;
    private final String IMAGE_FILE_NAME = "test.png";
    private final String TAG = "NewFragment";
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private FragmentNewBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i("NewFragment", "Initialize imageView Listener");

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
                            Log.d(TAG,"Photo has been taken");
                        }
                    }
                });
    }
    private void takePhoto() {
        Log.d(TAG, "Use system camera to take photo");
        // use Intent to access camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
        // new for FileProvider
        File imageFile = createPhotoFile();

        Uri photoURI = FileProvider.getUriForFile(getActivity(),"com.example.cleanfix.ui.new.fileprovider",imageFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        someActivityResultLauncher.launch(takePictureIntent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public File createPhotoFile() {
    // Check if external media is available
    if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
        Log.d(TAG, "Not mounted");
        return null;
    }
    File fileDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    Log.d(TAG, "Directory: " + fileDir);
    if (!fileDir.exists()) {
        Log.d(TAG, "Path did not exist");
        if (!fileDir.mkdirs()) {
            Log.d(TAG, "Something wrong with directory: " + fileDir);
        }
    }
    // Create file
    File imageFile = new File(fileDir + "/" + IMAGE_FILE_NAME);
    Log.d(TAG, "Dir: " + imageFile);
    photoFile = imageFile;
    return imageFile;
    }
}