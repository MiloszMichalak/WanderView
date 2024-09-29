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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    public static void allItemsLoaded(List<ImageModel> imageModels, RecyclerView recyclerView, Context context, ProgressBar progressBar) {
            ImageAdapter adapter = new ImageAdapter(context, imageModels);
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
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
}
