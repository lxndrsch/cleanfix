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
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final List<Uri> photoUris;
    private final int maxImages;
    public PhotoAdapter(List<Uri> photoUris, int maxImages) {
        this.photoUris = photoUris;
        this.maxImages = maxImages > 0 ? maxImages : photoUris.size();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photoUris.get(position);

        Glide.with(holder.imageView.getContext())
                .load(photoUri)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .into(holder.imageView);

        holder.removeButton.setOnClickListener(v -> {
            photoUris.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, photoUris.size());
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(photoUris.size(), maxImages);
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
