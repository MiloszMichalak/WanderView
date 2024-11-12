package com.example.wanderview.CommentModel;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wanderview.R;
import com.example.wanderview.UserProfileActivity;
import com.example.wanderview.Utility;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    List<CommentModel> commentList;
    Context context;
    long likeAmount;

    public CommentAdapter(List<CommentModel> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_card, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        CommentModel item = commentList.get(position);
        holder.bind(item, position, commentList);

        likeAmount = item.getLikes();
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        TextView commentContent, commentAuthor, commentDate, likeAmountTextView;
        ImageView userProfileImage, likeButton;
        RelativeLayout main;
        Context context;
        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentAuthor = itemView.findViewById(R.id.commentAuthor);
            commentDate = itemView.findViewById(R.id.commentDate);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeAmountTextView = itemView.findViewById(R.id.likeAmount);
            main = itemView.findViewById(R.id.main);
            this.context = context;
        }

        public void bind(CommentModel item, int position, List<CommentModel> commentsList) {
            commentAuthor.setText(item.getAuthor());

            loadUserImage(item);
            if (item.isUserLiked){
                likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
            }

            long likeAmount = item.getLikes();

            commentContent.setText(item.getText());
            Utility.secondsToDate(item.getSeconds(), context, commentDate);
            Utility.hideLikesIf0Comments(likeAmount, likeAmountTextView);

            setupGetToUserInfoListener(item);

            setupLikeButtonListener(item);
            setupDeleteCommentListener(item, position, commentsList, recyclerView);
        }

        private void setupDeleteCommentListener(CommentModel item, int position, List<CommentModel> commentList, RecyclerView recyclerView) {
            main.setOnLongClickListener(v -> {
                if (Utility.getCurrentUser().getUid().equals(item.getUid())) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.getMenuInflater().inflate(R.menu.comment_popup_menu, popupMenu.getMenu());

                    popupMenu.show();

                    popupMenu.setOnMenuItemClickListener(item1 -> {
                        if (item1.getItemId() == R.id.deleteComment) {

                            DatabaseReference databaseReference = Utility.getUsersPhotosCollectionReference().child(item.getAuthorPostId())
                                    .child(item.getPostId()).child("comments").child(item.getKey());

                            databaseReference.removeValue();
                            commentList.remove(position);
                            recyclerView.getAdapter().notifyItemRemoved(position);
                        }

                        return false;
                    });
                }
                return true;
            });
        }

        private void setupLikeButtonListener(CommentModel item) {
            likeButton.setOnClickListener(v -> {
                final long finalLikeAmount = item.getLikes();
                long updatedLikeAmount;
                DatabaseReference databaseReference = Utility.getUsersPhotosCollectionReference().child(item.getAuthorPostId()).child(item.getPostId())
                        .child("comments").child(item.getKey());

                if (!item.isUserLiked()){
                    likeButton.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction(() ->
                            likeButton.animate().scaleX(1f).scaleY(1f).setDuration(150).start());

                    likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                    updatedLikeAmount = finalLikeAmount + 1;

                    databaseReference.child("likes").child(Utility.getCurrentUser().getUid()).setValue(true);
                } else {
                    likeButton.clearColorFilter();
                    updatedLikeAmount = finalLikeAmount - 1;
                    databaseReference.child("likes").child(Utility.getCurrentUser().getUid()).removeValue();
                }
                item.isUserLiked = !(item.isUserLiked);

                updateLikeAmountDisplay(updatedLikeAmount);
                item.setLikes(updatedLikeAmount);
            });
        }

        private void updateLikeAmountDisplay(long likeAmount) {
            likeAmountTextView.animate().scaleX(1.2f).scaleY(1.0f).setDuration(200).withEndAction(() -> {
                if (likeAmount > 0){
                    likeAmountTextView.setVisibility(View.VISIBLE);
                    likeAmountTextView.setText(String.valueOf(likeAmount));
                    likeAmountTextView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                } else {
                    likeAmountTextView.setVisibility(View.INVISIBLE);
                }
            });
        }

        private void setupGetToUserInfoListener(CommentModel item) {
            View.OnClickListener listener = v -> {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("Author", item.getUid());
                context.startActivity(intent);
            };
            userProfileImage.setOnClickListener(listener);
            commentAuthor.setOnClickListener(listener);
        }

        private void loadUserImage(CommentModel item) {
            Glide.with(context)
                    .load(item.getProfileImageUrl())
                    .error(R.drawable.profile_default)
                    .circleCrop()
                    .into(userProfileImage);
        }
    }
}
