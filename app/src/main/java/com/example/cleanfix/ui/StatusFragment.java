package com.example.cleanfix.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanfix.R;
import com.example.cleanfix.adapter.ImageAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private ArrayList<String> imageUrls = new ArrayList<>();
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.status_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2)); // 2-column grid layout
        imageAdapter = new ImageAdapter(imageUrls);
        recyclerView.setAdapter(imageAdapter);

        storageReference = FirebaseStorage.getInstance().getReference().child("issues/");

        fetchImageUrls();
    }

    private void fetchImageUrls() {
        storageReference.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference fileRef : listResult.getItems()) {
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    imageUrls.add(uri.toString());
                                    imageAdapter.notifyDataSetChanged();
                                    Log.d(TAG, "Image URL added: " + uri.toString());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error getting image URL", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to fetch images", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching images", e);
                });
    }
}
