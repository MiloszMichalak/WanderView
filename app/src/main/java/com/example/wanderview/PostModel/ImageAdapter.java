package com.example.wanderview.PostModel;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
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

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<ImageModel> imageModels;
    private final Context context;
    private boolean isClickable;
    private final FragmentManager fragmentManager;
    private final RecyclerView recyclerView;
    private final LifecycleOwner lifecycleOwner;
    private List<ViewHolder> viewHolders = new ArrayList<>();
    private SparseArray<ExoPlayer> playerMap = new SparseArray<>();

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageModel item = imageModels.get(position);
        holder.bind(item, context, fragmentManager, isClickable, recyclerView, position, lifecycleOwner, imageModels);

        playerMap.put(position, holder.exoPlayer);
        if (item.getType().equals("video") && !viewHolders.contains(holder)) {
            viewHolders.add(holder);
        }
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    public void pauseAll() {
        for (ViewHolder holder : viewHolders) {
            if (holder.exoPlayer != null && holder.exoPlayer.isPlaying()) {
                holder.exoPlayer.pause();
            }
        }
    }

    public void resumeAll() {
        for (ViewHolder holder : viewHolders) {
            if (holder.exoPlayer != null) {
                holder.exoPlayer.play();
            }
        }
    }

    public void resetAll() {
        for (ViewHolder holder : viewHolders) {
            holder.exoPlayer.release();
        }
    }

    public void pauseVideoAtPosition(int position) {
        ExoPlayer player = playerMap.get(position);
        if (player != null && player.isPlaying()) {
            player.stop();
        }
    }

    public void resumeVideoAtPosition(int position) {
        ExoPlayer player = playerMap.get(position);
        if (player != null && !player.isPlaying()) {
            player.play();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView, userProfileImage, postOptions, commentButton, likeButton;
        final TextView imageTitle, imageAuthor, imageDate, likeAmountTextView, commentAmount;
        PlayerView playerView;
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
                imageView.setVisibility(View.GONE);
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

