package com.example.wanderview.PostModel;

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
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.wanderview.CommentModel.CommentFragment;
import com.example.wanderview.R;
import com.example.wanderview.UserListModel.UsersListActivity;
import com.example.wanderview.UserProfileActivity;
import com.example.wanderview.Utility;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<ImageModel> imageModels;
    final Context context;
    boolean isClickable;
    FragmentManager fragmentManager;
    DatabaseReference databaseReference;
    String uid;
    long likeAmmount;
    View.OnClickListener listener;
    RecyclerView recyclerView;
    LifecycleOwner lifecycleOwner;

    public ImageAdapter(Context context, List<ImageModel> imageModels, boolean isClickable, FragmentManager fragmentManager, RecyclerView recyclerView, LifecycleOwner lifecycleOwner){
        this.context = context;
        this.imageModels = imageModels;
        this.isClickable = isClickable;
        this.fragmentManager = fragmentManager;
        this.recyclerView = recyclerView;
        this.lifecycleOwner = lifecycleOwner;
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
                .fitCenter()
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))
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

        Utility.secondsToDate(item.getTimestamp(), context, holder.imageDate);

        Glide.with(context)
                .load(item.getUserProfileImage())
                .apply(RequestOptions.circleCropTransform())
                .error(R.drawable.profile_default)
                .into(holder.userProfileImage);


        holder.postOptions.setOnClickListener(v -> {
            PostOptionsFragment postOptionsFragment = PostOptionsFragment.newBottomFragment(item.getKey());

            fragmentManager.setFragmentResultListener("result", lifecycleOwner, (requestKey, result) -> {
                if (result.getBoolean("resultBool")){
                    imageModels.remove(position);
                    notifyItemRemoved(position);
                }
            });

            postOptionsFragment.show(fragmentManager, "postOptions");
        });

        if(isClickable){
            listener = v -> {
                holder.postOptions.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("Author", item.getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            };
        } else {
            listener = v -> {
                recyclerView.animate().translationX(-10).setDuration(100).withEndAction(() -> {
                    recyclerView.animate().translationX(10).setDuration(100).start();
                });
            };
        }
        holder.userProfileImage.setOnClickListener(listener);
        holder.imageAuthor.setOnClickListener(listener);

        if (item.isTemp() || item.getImageUrl() == null){
            holder.postOptions.setVisibility(View.INVISIBLE);
        }

        likeAmmount = item.getLikes();

        Utility.hideLikesIf0Comments(likeAmmount, holder.likeAmmount);

        if (item.isUserLiked){
            holder.likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
        }
        holder.likeButton.setColorFilter(null);

            holder.likeButton.setOnClickListener(v -> {
                likeAmmount = item.getLikes();
                databaseReference = Utility.getUsersPhotosCollectionReference().child(item.getUid()).child(item.getKey());

                    if (!item.isUserLiked){
                    holder.likeButton.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction(() ->
                            holder.likeButton.animate().scaleX(1f).scaleY(1f).setDuration(150).start());

                    holder.likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                    likeAmmount += 1;

                    databaseReference.child("likes").child(uid).setValue(true);
                    item.isUserLiked = true;
                }
                else {
                    holder.likeButton.setColorFilter(null);
                    likeAmmount -= 1;
                    databaseReference.child("likes").child(uid).removeValue();
                    item.isUserLiked = false;
                }

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

        holder.commentAmount.setText(String.valueOf(item.getCommentAmount()));

        holder.commentButton.setOnClickListener(v -> {
            CommentFragment commentFragment = CommentFragment.newCommentFragment(item.getKey(), item.getUid(), item.getAuthor());
            commentFragment.show(fragmentManager, "comments");
        });
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView imageTitle, imageAuthor, imageDate, likeAmmount, commentAmount;
        final ImageView userProfileImage, postOptions, commentButton;
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
            commentButton = itemView.findViewById(R.id.commentButton);
            commentAmount = itemView.findViewById(R.id.commentAmount);
        }
    }
}

