package com.example.wanderview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    FloatingActionButton addImageBtn;
    StorageReference profileImagesStorageReference;
    RecyclerView recyclerView;
    ImageButton userProfileSettings;
    ProgressBar progressBar;
    List<ImageModel> imageModels = new ArrayList<>();
    Uri userProfileImageUri;

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

        currentUser = Utility.getCurrentUser();

        addImageBtn = findViewById(R.id.addImageBtn);

        progressBar = findViewById(R.id.progressBar);

        profileImagesStorageReference = Utility.getUsersProfilePhotosReference();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://wanderview-8b391-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference().child("UsersPhotos");

        addImageBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AddingImageActivity.class)));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        userProfileSettings = findViewById(R.id.userProfileImage);

        profileImagesStorageReference.child(currentUser.getDisplayName()).getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .error(R.drawable.profile_default)
                    .into(userProfileSettings);
            userProfileImageUri = uri;
        });

        userProfileSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            if (currentUser.getPhotoUrl() != null){
                intent.putExtra("AuthorProfileImage", userProfileImageUri.toString());
            }
            startActivity(intent);
        });

        fetchImagesFromStorage(databaseReference);

    }


    public void fetchImagesFromStorage(DatabaseReference databaseReference){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageModels.clear();
                int totalUsers = (int) dataSnapshot.getChildrenCount();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){

                    for (DataSnapshot imageSnapshot : userSnapshot.getChildren()){
                        String imageUrl = imageSnapshot.child("url").getValue(String.class);
                        String title = imageSnapshot.child("title").getValue(String.class);
                        String author = imageSnapshot.child("author").getValue(String.class);

                        if (!author.equals(currentUser.getDisplayName())){
                            profileImagesStorageReference.child(author).getDownloadUrl().addOnFailureListener(e -> {
                                imageModels.add(new ImageModel(
                                        imageUrl,
                                        title != null ? title : getString(R.string.unknown_title),
                                        author,
                                        null));
                                Utility.checkIfAllItemsLoaded(totalUsers, imageModels, recyclerView, getApplicationContext(), progressBar);
                            }).addOnSuccessListener(uri -> {
                                imageModels.add(new ImageModel(
                                        imageUrl,
                                        title != null ? title : getString(R.string.unknown_title),
                                        author,
                                        uri));
                                Utility.checkIfAllItemsLoaded(totalUsers, imageModels, recyclerView, getApplicationContext(), progressBar);
                            });
                            Collections.shuffle(imageModels);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}