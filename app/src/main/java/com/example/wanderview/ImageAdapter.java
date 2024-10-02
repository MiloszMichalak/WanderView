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
import java.util.concurrent.TimeUnit;

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

        long seconds = Timestamp.now().getSeconds() - item.getTimestamp();
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.MINUTES.toHours(minutes);
        long days = TimeUnit.HOURS.toDays(hours);

        if (seconds < 60){
            holder.imageDate.setText(context.getString(R.string.time_in_seconds, seconds));
        } else if (minutes < 60) {
            holder.imageDate.setText(context.getString(R.string.time_in_minutes, minutes));
        } else if (hours < 24){
            holder.imageDate.setText(context.getString(R.string.time_in_hours, hours));
        } else if (days < 7){
            holder.imageDate.setText(context.getString(R.string.time_in_days, days));
        } else {
            holder.imageDate.setText(Utility.timestampToDate(seconds, "dd:MM"));
        }

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

