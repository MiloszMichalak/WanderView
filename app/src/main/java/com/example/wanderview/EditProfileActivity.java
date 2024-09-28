package com.example.wanderview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
public class EditProfileActivity extends AppCompatActivity {

    TextInputEditText usernameEdit;
    ImageButton userProfileImage;
    FirebaseUser currentUser;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;
    MaterialButton saveInfo;
    StorageReference storageReference;

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

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == EditProfileActivity.RESULT_OK){
                Intent data = result.getData();
                if (data != null && data.getData() != null){
                    selectedImageUri = data.getData();
                }
            }
        });
        // TODO zmiana nicku zeby dzialala
        usernameEdit = findViewById(R.id.usernameEdit);

        userProfileImage = findViewById(R.id.userProfileImage);

        saveInfo = findViewById(R.id.saveInfo);

        currentUser = Utility.getCurrentUser();

        storageReference = Utility.getUsersProfilePhotosReference().child(currentUser.getDisplayName());

        usernameEdit.setText(currentUser.getDisplayName());

        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .error(R.drawable.profile_default)
                    .into(userProfileImage);
        });

        userProfileImage.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(4096)
                    .maxResultSize(200, 200)
                    .start();
        });

        saveInfo.setOnClickListener(v -> storageReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> finish()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() != null){
            selectedImageUri = data.getData();

            Glide.with(this)
                    .load(selectedImageUri)
                    .into(userProfileImage);
        }
    }
}