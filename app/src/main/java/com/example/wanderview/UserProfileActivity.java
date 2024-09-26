package com.example.wanderview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    ImageView profilePicture;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    TextView username;
    FirebaseStorage storage;
    StorageReference storageReference;
    RecyclerView recyclerView;
    MaterialButton editProfileBtn;

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

        recyclerView = findViewById(R.id.imageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference( "UsersPhotos/"+currentUser.getDisplayName()+"/");

        profilePicture = findViewById(R.id.imageView);
        username = findViewById(R.id.userName);

        editProfileBtn = findViewById(R.id.editProfileBtn);
        editProfileBtn.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        Glide.with(this)
                .load(currentUser.getPhotoUrl())
                .error(R.drawable.profile_default)
                .into(profilePicture);

        username.setText(currentUser.getDisplayName());

        fetchImagesFromStorage(storageReference);
    }

    public void fetchImagesFromStorage(StorageReference storageReference){
        List<ImageModel> imageModels = new ArrayList<>();
        storageReference.listAll().addOnSuccessListener(listResult -> {
            int totalItems = listResult.getItems().size();
            if (totalItems==0){return;}
            for (StorageReference item : listResult.getItems()){
                item.getMetadata().addOnSuccessListener(storageMetadata -> {
                    String title = storageMetadata.getCustomMetadata("title");
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                       imageModels.add(new ImageModel(uri.toString(),
                               title != null ? title : getString(R.string.unknown_title),
                               currentUser.getDisplayName(),
                               currentUser.getPhotoUrl()));
                       ImageAdapter adapter = new ImageAdapter(getApplicationContext(), imageModels);
                       recyclerView.setAdapter(adapter);
                   });
                });
            }
        });
    }
}