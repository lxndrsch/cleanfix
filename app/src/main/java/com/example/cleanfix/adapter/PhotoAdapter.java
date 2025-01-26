package com.example.cleanfix.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.cleanfix.R;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final ArrayList<Uri> photoUris;

    public PhotoAdapter(ArrayList<Uri> photoUris) {
        this.photoUris = photoUris;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each photo
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photoUris.get(position);

        // Use Glide to load the image with error handling and caching options
        Glide.with(holder.imageView.getContext())
                .load(photoUri)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache optimization
                        .centerCrop()) // Scale type for the image
                .into(holder.imageView);

        // Set click listener for the remove button
        holder.removeButton.setOnClickListener(v -> {
            photoUris.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, photoUris.size());
        });
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView removeButton;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_image_view);
            removeButton = itemView.findViewById(R.id.remove_image_button);
        }
    }
}
