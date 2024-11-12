package com.example.wanderview.UserListModel;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wanderview.R;
import com.example.wanderview.UserProfileActivity;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    List<UserModel> userModels;
    final Context context;

    public UserAdapter(Context context, List<UserModel> userModels) {
        this.context = context;
        this.userModels = userModels;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        UserModel item = userModels.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImage;
        TextView imageAuthor;
        RelativeLayout main;
        Context context;
        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            imageAuthor = itemView.findViewById(R.id.imageAuthor);
            main = itemView.findViewById(R.id.main);
            this.context = context;
        }

        public void bind(UserModel item) {
            imageAuthor.setText(item.getUsername());

            loadUserImage(item);

            main.setOnClickListener(v -> openUserProfile(item.getAuthor()));
        }

        private void openUserProfile(String authorId) {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("Author", authorId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        private void loadUserImage(UserModel item) {
            Glide.with(context)
                    .load(item.getProfilePhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .error(R.drawable.profile_default)
                    .into(userProfileImage);
        }
    }
}
