package com.example.wanderview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
public class EditProfileActivity extends AppCompatActivity {

    TextInputLayout usernameEditLayout;
    EditText usernameEdit;
    ImageView userProfileImage;
    FirebaseUser currentUser;
    Uri profileImageUri;
    MaterialButton saveInfo;
    StorageReference originalStorageReference;
    String originalUsername;
    boolean isPhotoChanged;
    DatabaseReference infoDatabaseReference;
    String photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditLayout = findViewById(R.id.usernameEditLayout);
        usernameEdit = usernameEditLayout.getEditText();

        userProfileImage = findViewById(R.id.userProfileImage);

        saveInfo = findViewById(R.id.saveInfo);

        currentUser = Utility.getCurrentUser();

        infoDatabaseReference = Utility.getUsersInfoCollectionReference().child(currentUser.getUid());

        infoDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                originalUsername = snapshot.child("username").getValue(String.class);
                usernameEdit.setText(originalUsername);

                photoUrl = snapshot.child("photoUrl").getValue(String.class);
                Glide.with(getApplicationContext())
                        .load(photoUrl)
                        .error(R.drawable.profile_default)
                        .into(userProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        originalStorageReference = Utility.getUsersProfilePhotosReference().child(currentUser.getUid());

        userProfileImage.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(4096)
                    .maxResultSize(200, 200)
                    .start();
        });

        Utility.disableButton(usernameEdit, saveInfo);

        saveInfo.setOnClickListener(v -> {
            String username = usernameEdit.getText().toString();

            if (!username.equals(originalUsername)){
                infoDatabaseReference.child("username").setValue(username);
            }

            if (isPhotoChanged){
                originalStorageReference.putFile(profileImageUri).addOnSuccessListener(taskSnapshot -> {
                    originalStorageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                       infoDatabaseReference.child("photoUrl").setValue(uri.toString());
                    });
                });
            }
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() != null){
            profileImageUri = data.getData();
            isPhotoChanged = true;

            Glide.with(this)
                    .load(profileImageUri)
                    .into(userProfileImage);
        }
    }
}