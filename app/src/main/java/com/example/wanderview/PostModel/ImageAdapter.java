package com.example.wanderview.PostModel;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
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
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<ImageModel> imageModels;
    final Context context;
    boolean isClickable;
    FragmentManager fragmentManager;
    RecyclerView recyclerView;
    LifecycleOwner lifecycleOwner;
    List<ImageAdapter.ViewHolder> viewHolders = new ArrayList<>();

    public ImageAdapter(Context context, List<ImageModel> imageModels, boolean isClickable, FragmentManager fragmentManager, RecyclerView recyclerView, LifecycleOwner lifecycleOwner) {
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
        ImageModel item = imageModels.get(position);
        holder.bind(item, context, fragmentManager, isClickable, recyclerView, position, lifecycleOwner, imageModels);

        if (item.getType().equals("video") && !viewHolders.contains(holder)){
            viewHolders.add(holder);
        }
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    public void pauseAll(){
        for (ImageAdapter.ViewHolder holder : viewHolders) {
            if (holder.exoPlayer != null && holder.exoPlayer.isPlaying()){
                holder.exoPlayer.pause();
            }
        }
    }

    public void resumeAll(){
        for (ImageAdapter.ViewHolder holder : viewHolders) {
            if (holder.exoPlayer != null){
                holder.exoPlayer.play();
            }
        }
    }

    public void resetAll(){
        for (ImageAdapter.ViewHolder holder : viewHolders){
            holder.exoPlayer.release();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView, userProfileImage, postOptions, commentButton, likeButton;
        final TextView imageTitle, imageAuthor, imageDate, likeAmountTextView, commentAmount;
        StyledPlayerView playerView;
        ExoPlayer exoPlayer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            playerView = itemView.findViewById(R.id.playerView);
            imageTitle = itemView.findViewById(R.id.imageTitle);
            imageAuthor = itemView.findViewById(R.id.imageAuthor);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            imageDate = itemView.findViewById(R.id.imageDate);
            postOptions = itemView.findViewById(R.id.postOptions);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeAmountTextView = itemView.findViewById(R.id.likeAmmount);
            commentButton = itemView.findViewById(R.id.commentButton);
            commentAmount = itemView.findViewById(R.id.commentAmount);
        }

        public void bind(ImageModel imageModel, Context context, FragmentManager fragmentManager, boolean isClickable, RecyclerView recyclerView, int position, LifecycleOwner lifecycleOwner, List<ImageModel> imageModels) {
            loadImage(context, imageModel);
            bindTitle(imageModel);
            bindAuthor(context, imageModel);
            bindLikes(context, imageModel);
            bindComments(imageModel, fragmentManager);
            bindUserPhotoListener(imageModel, context, isClickable, recyclerView);
            bindPostOptionsListener(imageModel, position, fragmentManager, lifecycleOwner, recyclerView, imageModels);
            bindLikeAmountListener(context, imageModel);
        }

        private void bindLikeAmountListener(Context context, ImageModel item) {
            likeAmountTextView.setOnClickListener(v -> {
                Intent intent = new Intent(context, UsersListActivity.class);
                intent.putExtra("author", item.getUid());
                intent.putExtra("postKey", item.getKey());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }

        private void bindPostOptionsListener(ImageModel item, int position, FragmentManager fragmentManager, LifecycleOwner lifecycle, RecyclerView recyclerView, List<ImageModel> imageModels) {
            if (Utility.getCurrentUser().getUid().equals(item.getUid())) {
                postOptions.setVisibility(View.VISIBLE);
            }

            postOptions.setOnClickListener(v -> {
                PostOptionsFragment postOptionsFragment = PostOptionsFragment.newBottomFragment(item.getKey());

                fragmentManager.setFragmentResultListener("result", lifecycle, (requestKey, result) -> {
                    if (result.getBoolean("resultBool")) {
                        imageModels.remove(position);
                        recyclerView.getAdapter().notifyItemRemoved(position);
                    }
                });

                postOptionsFragment.show(fragmentManager, "postOptions");
            });
        }

        private void bindUserPhotoListener(ImageModel item, Context context, boolean isClickable, RecyclerView recyclerView) {
            View.OnClickListener listener;
            if (isClickable) {
                listener = v -> {
                    postOptions.setVisibility(View.INVISIBLE);
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
            userProfileImage.setOnClickListener(listener);
            imageAuthor.setOnClickListener(listener);
        }

        private void bindComments(ImageModel item, FragmentManager fragmentManager) {
            commentAmount.setText(String.valueOf(item.getCommentAmount()));

            commentButton.setOnClickListener(v -> {
                CommentFragment commentFragment = CommentFragment.newCommentFragment(item.getKey(), item.getUid(), item.getAuthor());
                commentFragment.show(fragmentManager, "comments");
            });
        }

        private void bindLikes(Context context, ImageModel item) {
            long likeAmount = item.getLikes();
            Utility.hideLikesIf0Comments(likeAmount, likeAmountTextView);
            if (item.isUserLiked) {
                likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
            } else {
                likeButton.clearColorFilter();
            }

            likeButton.setOnClickListener(v -> {
                long updatedLikeAmount;
                final long currentLikeAmount = item.getLikes();
                DatabaseReference databaseReference = Utility.getUsersPhotosCollectionReference().child(item.getUid()).child(item.getKey());

                if (!item.isUserLiked) {
                    likeButton.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction(() -> likeButton.animate().scaleX(1f).scaleY(1f).setDuration(150).start());

                    likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                    updatedLikeAmount = currentLikeAmount + 1;

                    databaseReference.child("likes").child(Utility.getCurrentUser().getUid()).setValue(true);
                    item.isUserLiked = true;
                } else {
                    likeButton.clearColorFilter();
                    updatedLikeAmount = currentLikeAmount - 1;
                    databaseReference.child("likes").child(Utility.getCurrentUser().getUid()).removeValue();
                    item.isUserLiked = false;
                }

                updateLikeAmountDisplay(updatedLikeAmount);
                item.setLikes(updatedLikeAmount);
            });
        }

        private void updateLikeAmountDisplay(long likeAmount) {
            likeAmountTextView.animate().translationY(-100f).setDuration(200).withEndAction(() -> {
                if (likeAmount > 0) {
                    likeAmountTextView.setVisibility(View.VISIBLE);
                    likeAmountTextView.setText(String.valueOf(likeAmount));
                    likeAmountTextView.animate().translationY(0).setDuration(300).start();
                } else {
                    likeAmountTextView.setVisibility(View.INVISIBLE);
                }
            });
        }

        private void bindAuthor(Context context, ImageModel item) {
            imageAuthor.setText(item.getAuthor());

            Utility.secondsToDate(item.getTimestamp(), context, imageDate);

            Glide.with(context).load(item.getUserProfileImage()).apply(RequestOptions.circleCropTransform()).error(R.drawable.profile_default).into(userProfileImage);
        }

        private void bindTitle(ImageModel item) {
            if (item.getTitle() != null && item.getTitle().isEmpty()) {
                imageTitle.setVisibility(View.GONE);
            } else {
                imageTitle.setText(item.getTitle());
                imageTitle.setVisibility(View.VISIBLE);
            }
        }

        private void loadImage(Context context, ImageModel item) {
            if (item.getType().equals("video")) {
                imageView.setVisibility(View.INVISIBLE);
                playerView.setVisibility(View.VISIBLE);

                exoPlayer = new ExoPlayer.Builder(context).build();
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

                playerView.setPlayer(exoPlayer);

                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(item.getImageUrl()));

                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
            } else {
                playerView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(item.getImageUrl()).fitCenter()
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))
                        .into(imageView);
            }
        }
    }

}

