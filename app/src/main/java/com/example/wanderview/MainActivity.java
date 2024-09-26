package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FloatingActionButton addImageBtn;
    FirebaseStorage storage;
    StorageReference storageReference, profileImagesStorageReference;
    RecyclerView recyclerView;
    ImageButton userProfileSettings;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        addImageBtn = findViewById(R.id.addImageBtn);

        progressBar = findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("UsersPhotos/");
        // TODO pozamykac referencje w utilsy
        // TODO rewizja kodu
        profileImagesStorageReference = storage.getReference().child("UsersProfilePhotos/");
        // TODO jesli user nie ma profilowego ma po prostu ladowac default a nie wywalac bleda

        addImageBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AddingImageActivity.class)));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        userProfileSettings = findViewById(R.id.userProfileImage);

        Glide.with(this).load(currentUser.getPhotoUrl()).error(R.drawable.profile_default).into(userProfileSettings);

        userProfileSettings.setOnClickListener(v -> startActivity(new Intent(this, UserProfileActivity.class)));

        fetchImagesFromStorage(storageReference);
    }

    List<ImageModel> imageModels = new ArrayList<>();

    public void fetchImagesFromStorage(StorageReference storageReference){
        storageReference.listAll().addOnSuccessListener(listResult -> {
            int totalItems = listResult.getItems().size();
            for (StorageReference item : listResult.getItems()){
                item.getMetadata().addOnSuccessListener(metadata -> {
                    String title = metadata.getCustomMetadata("title");
                    String author = metadata.getCustomMetadata("author");
                    if (!author.equals(currentUser.getDisplayName())){
                        item.getDownloadUrl().addOnSuccessListener(uri -> profileImagesStorageReference.child(author).getDownloadUrl().addOnFailureListener(e -> {
                            imageModels.add(new ImageModel(uri.toString(),
                                    title != null ? title : getString(R.string.unknown_title),
                                    author,
                                    null));
                            if (imageModels.size() == totalItems) {
                                ImageAdapter adapter = new ImageAdapter(getApplicationContext(), imageModels);
                                recyclerView.setAdapter(adapter);
                            }
                            // TODO tzeba naprawic to cos
                        }).addOnSuccessListener(uri1 -> {
                            imageModels.add(new ImageModel(uri.toString(),
                                    title != null ? title : getString(R.string.unknown_title),
                                    author,
                                    uri1));
                            if (imageModels.size() == totalItems) {
                                ImageAdapter adapter = new ImageAdapter(getApplicationContext(), imageModels);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }));
                    }
                });
            }
            for (StorageReference prefix : listResult.getPrefixes()){
                fetchImagesFromStorage(prefix);
            }
        }).addOnFailureListener(e -> {

        });
    }
}