package com.example.wanderview;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<ImageModel> imageModels;
    final Context context;
    boolean isClickable;
    FragmentManager fragmentManager;
    boolean isLikeClicked;
    DatabaseReference databaseReference;
    String uid;
    int likeAmmount;

    public ImageAdapter(Context context, List<ImageModel> imageModels, boolean isClickable, FragmentManager fragmentManager){
        this.context = context;
        this.imageModels = imageModels;
        this.isClickable = isClickable;
        this.fragmentManager = fragmentManager;
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

        uid = Utility.getCurrentUser().getUid();

        Glide.with(context)
                .load(item.getImageUrl())
                .into(holder.imageView);

        if (item.getTitle() != null && item.getTitle().isEmpty()) {
            holder.imageTitle.setVisibility(View.GONE);
        } else {
            holder.imageTitle.setText(item.getTitle());
            holder.imageTitle.setVisibility(View.VISIBLE);
        }

        if (uid.equals(item.getUid())){
            holder.postOptions.setVisibility(View.VISIBLE);
        }

        holder.imageAuthor.setText(item.getAuthor());

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
                .apply(RequestOptions.circleCropTransform())
                .error(R.drawable.profile_default)
                .into(holder.userProfileImage);

        // todo shake jak sie kliknie juz nie do klikniecia imageview

        holder.postOptions.setOnClickListener(v -> {
            PostOptionsFragment postOptionsFragment = PostOptionsFragment.newBottomFragment(item.getKey());
            postOptionsFragment.show(fragmentManager, "postOptions");
        });

        if(isClickable){
            holder.userProfileImage.setOnClickListener(v -> {
                holder.postOptions.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("Author", item.getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }

        likeAmmount = item.getLikes();

        if (likeAmmount > 0){
            holder.likeAmmount.setText(String.valueOf(item.getLikes()));
        } else {
            holder.likeAmmount.setVisibility(View.INVISIBLE);
        }

        isLikeClicked = item.isUserLiked();
        if (item.isUserLiked()){
            holder.likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
        }

        holder.likeButton.setOnClickListener(v -> {
            databaseReference = Utility.getUsersPhotosCollectionReference().child(item.getUid()).child(item.getKey());
            likeAmmount = item.getLikes();

            if (!item.isUserLiked()){
                holder.likeButton.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction(() ->
                        holder.likeButton.animate().scaleX(1f).scaleY(1f).setDuration(150).start());

                holder.likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                likeAmmount += 1;

                if (likeAmmount == 1) {
                    holder.likeAmmount.setVisibility(View.VISIBLE);
                }

                databaseReference.child("likes").child(uid).setValue(true);
                item.isUserLiked = true;
            } else {
                holder.likeButton.setColorFilter(null);
                likeAmmount -= 1;
                databaseReference.child("likes").child(uid).removeValue();
                item.isUserLiked = false;
            }

            databaseReference.child("likeAmmount").setValue(likeAmmount);

            holder.likeAmmount.animate().translationY(-100f).setDuration(200).withEndAction(() -> {
                if (likeAmmount > 0){
                    holder.likeAmmount.setVisibility(View.VISIBLE);
                    holder.likeAmmount.setText(String.valueOf(likeAmmount));
                    holder.likeAmmount.animate().translationY(0).setDuration(300).start();
                } else {
                    holder.likeAmmount.setVisibility(View.INVISIBLE);
                }
            });


            item.setLikes(likeAmmount);
        });

        holder.likeAmmount.setOnClickListener(v -> {
            Intent intent = new Intent(context, UsersListActivity.class);
            intent.putExtra("author", item.getUid());
            intent.putExtra("postKey", item.getKey());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView imageTitle, imageAuthor, imageDate, likeAmmount;
        final ImageView userProfileImage, postOptions;
        ImageView likeButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            imageTitle = itemView.findViewById(R.id.imageTitle);
            imageAuthor = itemView.findViewById(R.id.imageAuthor);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            imageDate = itemView.findViewById(R.id.imageDate);
            postOptions = itemView.findViewById(R.id.postOptions);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeAmmount = itemView.findViewById(R.id.likeAmmount);
        }
    }
}

