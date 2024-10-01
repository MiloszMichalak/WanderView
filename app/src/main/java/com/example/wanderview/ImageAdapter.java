package com.example.wanderview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<ImageModel> imageModels;
    final Context context;
    boolean isClickable = true;

    public ImageAdapter(Context context, List<ImageModel> imageModels, boolean isClickable){
        this.context = context;
        this.imageModels = imageModels;
        this.isClickable = isClickable;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        holder.userProfileImage.setEnabled(isClickable);

        ImageModel item = imageModels.get(position);

        Glide.with(context)
                .load(item.getImageUrl())
                .into(holder.imageView);

        if (item.getTitle() != null){
            holder.textView.setText(item.getTitle());
        } else {
            holder.textView.setVisibility(View.INVISIBLE);
        }
        holder.textView2.setText(item.getAuthor());
        if (Timestamp.now().getSeconds() - item.getTimestamp() > 3600){
            holder.imageDate.setText(Utility.timestampToDate(item.getTimestamp(), "dd:MM"));
        } else {
            holder.imageDate.setText(Utility.timestampToDate(item.getTimestamp(), "HH:mm"));
        }

        // todo system bardziej ze ile godzin do 24h ile dni do jakis 7 dni i takie rzeczy

        Glide.with(context)
                .load(item.getUserProfileImage())
                .error(R.drawable.profile_default)
                .into(holder.userProfileImage);

        // todo shake jak sie kliknie juz nie do klikniecia imageview

        if(isClickable){
            holder.userProfileImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("Author", item.getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView textView, textView2, imageDate;
        final ImageView userProfileImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.imageTitle);
            textView2 = itemView.findViewById(R.id.imageAuthor);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            imageDate = itemView.findViewById(R.id.imageDate);
        }
    }
}

