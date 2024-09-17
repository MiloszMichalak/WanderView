package com.example.wanderview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<ImageModel> imageModels;
    Context context;

    public ImageAdapter(Context context, List<ImageModel> imageModels){
        this.context = context;
        this.imageModels = imageModels;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        ImageModel item = imageModels.get(position);

        Glide.with(context)
                .load(item.getImageUrl())
                .error(R.drawable.default_image)
                .into(holder.imageView);

        holder.textView.setText(item.getTitle());
        holder.textView2.setText(item.getAuthor());
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView, textView2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.imageTitle);
            textView2 = itemView.findViewById(R.id.imageAuthor);
        }
    }
}

