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
    DatabaseReference databaseReference;
    long likeAmount;
    String uid;

    public CommentAdapter(List<CommentModel> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_model, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        CommentModel item = commentList.get(position);

        holder.commentAuthor.setText(item.getAuthor());

        uid = Utility.getCurrentUser().getUid();

        Glide.with(context)
                .load(item.getProfileImageUrl())
                .error(R.drawable.profile_default)
                .circleCrop()
                .into(holder.userProfileImage);

        holder.commentContent.setText(item.getText());

        Utility.secondsToDate(item.getSeconds(), context, holder.commentDate);

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("Author", item.getUid());
            context.startActivity(intent);
        };
        holder.userProfileImage.setOnClickListener(listener);
        holder.commentAuthor.setOnClickListener(listener);

        likeAmount = item.getLikes();

        Utility.hideLikesIf0Comments(likeAmount, holder.likeAmount);

        if (item.isUserLiked){
            holder.likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
        }

        holder.likeButton.setOnClickListener(v -> {
            likeAmount = item.getLikes();
            databaseReference = Utility.getUsersPhotosCollectionReference().child(item.getAuthorPostId()).child(item.getPostId())
                    .child("comments").child(item.getKey());

            if (!item.isUserLiked()){
                holder.likeButton.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction(() ->
                        holder.likeButton.animate().scaleX(1f).scaleY(1f).setDuration(150).start());

                holder.likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
                likeAmount += 1;

                databaseReference.child("likes").child(uid).setValue(true);
            } else {
                holder.likeButton.setColorFilter(null);
                likeAmount -= 1;
                databaseReference.child("likes").child(uid).removeValue();
            }
            item.isUserLiked = !(item.isUserLiked);

            holder.likeAmount.animate().scaleX(1.2f).scaleY(1.0f).setDuration(200).withEndAction(() -> {
                if (likeAmount > 0){
                    holder.likeAmount.setVisibility(View.VISIBLE);
                    holder.likeAmount.setText(String.valueOf(likeAmount));
                    holder.likeAmount.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                } else {
                    holder.likeAmount.setVisibility(View.INVISIBLE);
                }
            });
            item.setLikes(likeAmount);
        });

        holder.main.setOnLongClickListener(v -> {
            if (uid.equals(item.getUid())) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.comment_popup_menu, popupMenu.getMenu());

                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(item1 -> {
                    if (item1.getItemId() == R.id.deleteComment) {

                        databaseReference = Utility.getUsersPhotosCollectionReference().child(item.getAuthorPostId())
                                .child(item.getPostId()).child("comments").child(item.getKey());

                        databaseReference.removeValue();
                        commentList.remove(position);
                        notifyItemRemoved(position);
                    }

                    return false;
                });
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        TextView commentContent, commentAuthor, commentDate, likeAmount;
        ImageView userProfileImage, likeButton;
        RelativeLayout main;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentAuthor = itemView.findViewById(R.id.commentAuthor);
            commentDate = itemView.findViewById(R.id.commentDate);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeAmount = itemView.findViewById(R.id.likeAmount);
            main = itemView.findViewById(R.id.main);
        }
    }
}
