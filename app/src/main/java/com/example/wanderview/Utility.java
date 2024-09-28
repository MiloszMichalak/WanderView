package com.example.wanderview;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class Utility {
    public static boolean isValidEmail(CharSequence email){
        return (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static boolean isValidPassword(CharSequence password){
        return (TextUtils.isEmpty(password) || password.length() < 8);
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

    public static void checkIfAllItemsLoaded(int totalItems, List<ImageModel> imageModels, RecyclerView recyclerView, Context context, ProgressBar progressBar) {
        if (imageModels.size() == totalItems) {
            ImageAdapter adapter = new ImageAdapter(context, imageModels);
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
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
}
