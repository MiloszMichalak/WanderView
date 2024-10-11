package com.example.wanderview;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wanderview.CommentModel.CommentAdapter;
import com.example.wanderview.CommentModel.CommentModel;
import com.example.wanderview.PostModel.ImageAdapter;
import com.example.wanderview.PostModel.ImageModel;
import com.example.wanderview.UserListModel.UserAdapter;
import com.example.wanderview.UserListModel.UserModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utility {
    public static boolean isValidEmail(CharSequence email){
        return (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static boolean isValidPassword(CharSequence password){
        return (TextUtils.isEmpty(password) || password.length() < 8);
    }

    public static boolean isUsernameValid(CharSequence username) {
        return (TextUtils.isEmpty(username) || username.length() > 20 || username.length() < 3);
    }

    public static FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static StorageReference getUsersPhotosReference(){
        return FirebaseStorage.getInstance().getReference("UsersPhotos/");
    }

    public static StorageReference getUsersProfilePhotosReference(){
        return FirebaseStorage.getInstance().getReference("UsersProfilePhotos/");
    }

    public static DatabaseReference getUsersPhotosCollectionReference(){
        return FirebaseDatabase.getInstance("https://wanderview-8b391-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("UsersPhotos");
    }

    public static DatabaseReference getUsersInfoCollectionReference(){
        return FirebaseDatabase.getInstance("https://wanderview-8b391-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("UsersInfo");
    }

    public static String timestampToDate(long timestamp, String format){
        Date date = new Date(timestamp * 1000);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return simpleDateFormat.format(date);
    }

    public static void allImagesLoaded(List<ImageModel> imageModels, RecyclerView recyclerView, Context context, ProgressBar progressBar, boolean isClickable,
                                       FragmentManager fragmentManager){
        ImageAdapter adapter = new ImageAdapter(context, imageModels, isClickable, fragmentManager, recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public static void allUsersLoaded(List<UserModel> array, RecyclerView recyclerView, Context context) {
        UserAdapter adapter = new UserAdapter(context, array);
        recyclerView.setAdapter(adapter);
    }

    public static void allCommentsLoaded(List<CommentModel> array, RecyclerView recyclerView, Context context) {
        CommentAdapter adapter = new CommentAdapter(array, context);
        recyclerView.setAdapter(adapter);
    }

    public static void secondsToDate(long secondsTimestamp, Context context, TextView imageDate){
        long seconds = Timestamp.now().getSeconds() - secondsTimestamp;
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.MINUTES.toHours(minutes);
        long days = TimeUnit.HOURS.toDays(hours);

        if (seconds < 60){
            imageDate.setText(context.getString(R.string.time_in_seconds, seconds));
        } else if (minutes < 60) {
            imageDate.setText(context.getString(R.string.time_in_minutes, minutes));
        } else if (hours < 24){
            imageDate.setText(context.getString(R.string.time_in_hours, hours));
        } else if (days < 7){
            imageDate.setText(context.getString(R.string.time_in_days, days));
        } else {
            imageDate.setText(Utility.timestampToDate(seconds, "dd:MM"));
        }
    }

    public static void disableButton(EditText usernameEdit, MaterialButton saveInfo){
        usernameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0){
                    saveInfo.setEnabled(false);
                    saveInfo.setAlpha(0.5f);
                } else {
                    saveInfo.setEnabled(true);
                    saveInfo.setAlpha(1f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public static void deleteError(EditText editText, TextInputLayout textInputLayout){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public static void hideLikesIf0Comments(long likesCount, TextView likeAmount) {
        if (likesCount > 0){
            likeAmount.setText(String.valueOf(likesCount));
        } else {
            likeAmount.setVisibility(View.INVISIBLE);
        }
    }

    public static View.OnClickListener listenerForCommentUser(Context context, String uid){
        return v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("Author", uid);
            context.startActivity(intent);
        };
    }
}
