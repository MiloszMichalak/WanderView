package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    FloatingActionButton addImageBtn;
    RecyclerView recyclerView;
    ImageButton userProfileSettings;
    ProgressBar progressBar;
    List<ImageModel> imageModels = new ArrayList<>();
    DatabaseReference databaseReference, infoDatabaseReference;
    String username;
    String photoUrl;
    SwipeRefreshLayout swipeRefreshLayout;

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

        databaseReference = Utility.getUsersPhotosCollectionReference();
        infoDatabaseReference = Utility.getUsersInfoCollectionReference();

        infoDatabaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                username = snapshot.child("username").getValue(String.class);
                photoUrl = snapshot.child("photoUrl").getValue(String.class);
                Glide.with(getApplicationContext())
                        .load(photoUrl)
                        .error(R.drawable.profile_default)
                        .into(userProfileSettings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addImageBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AddingImageActivity.class)));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        userProfileSettings = findViewById(R.id.userProfileImage);

        userProfileSettings.setOnClickListener(v -> startActivity(new Intent(this, UserProfileActivity.class)));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                swipeRefreshLayout.setEnabled(!recyclerView.canScrollVertically(-1));
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorSecondary);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            fetchImagesFromStorage(databaseReference);
            swipeRefreshLayout.setRefreshing(false);
        });

        fetchImagesFromStorage(databaseReference);
    }


    public void fetchImagesFromStorage(DatabaseReference databaseReference){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageModels.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){

                    for (DataSnapshot imageSnapshot : userSnapshot.getChildren()){
                        String imageUrl = imageSnapshot.child("url").getValue(String.class);
                        String title = imageSnapshot.child("title").getValue(String.class);
                        String author = imageSnapshot.child("author").getValue(String.class);
                        Long date = imageSnapshot.child("date").getValue(Long.class);
                        String key = imageSnapshot.getKey();

                        if (!author.equals(currentUser.getUid())){
                            boolean likedByCurrentUser = imageSnapshot.child("likes").child(currentUser.getUid()).exists();
                            infoDatabaseReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int likes = imageSnapshot.child("likeAmmount").getValue(Integer.class);

                                    imageModels.add(new ImageModel(
                                            imageUrl,
                                            title,
                                            snapshot.child("username").getValue(String.class),
                                            snapshot.child("photoUrl").getValue(String.class),
                                            author,
                                            key,
                                            date,
                                            likes,
                                            likedByCurrentUser
                                    ));
                                        Utility.allItemsLoaded(imageModels, recyclerView, getApplicationContext(), progressBar, true, getSupportFragmentManager());
                                        Collections.shuffle(imageModels);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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