package com.example.wanderview;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserProfileActivity extends AppCompatActivity {

    ImageView profilePicture;
    Uri pictureUri;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    TextView username;
    FirebaseStorage storage;
    StorageReference storageReference;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(currentUser.getDisplayName()+"/");

        profilePicture = findViewById(R.id.imageView);
        username = findViewById(R.id.userName);

        Glide.with(this).load(currentUser.getPhotoUrl()).error(R.drawable.profile_default).into(profilePicture);
        username.setText(currentUser.getDisplayName());


        ActivityResultLauncher<PickVisualMediaRequest> pickImage =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->{
                    if (uri != null){
                        profilePicture.setImageURI(uri);
                        pictureUri = uri;
                    }
                });
    }
}