package com.example.wanderview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    ImageView profilePicture;
    FirebaseUser currentUser;
    TextView usernameText;
    DatabaseReference databaseReference, infoDatabaseReference;
    RecyclerView recyclerView;
    MaterialButton editProfileBtn;
    ProgressBar progressBar;
    String author;
    String username;
    Uri profilePictureUri;
    List<ImageModel> imageModels = new ArrayList<>();


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

        currentUser = Utility.getCurrentUser();

        recyclerView = findViewById(R.id.imageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        Intent intent = getIntent();
        editProfileBtn = findViewById(R.id.editProfileBtn);

        infoDatabaseReference = Utility.getUsersInfoCollectionReference();
        databaseReference = Utility.getUsersPhotosCollectionReference().child(currentUser.getUid());

        if (intent.getStringExtra("Author") != null){
            author = intent.getStringExtra("Author");
            editProfileBtn.setVisibility(View.INVISIBLE);
        }

        if (intent.getStringExtra("AuthorProfileImage") != null){
            profilePictureUri = Uri.parse(intent.getStringExtra("AuthorProfileImage"));
        }

        profilePicture = findViewById(R.id.imageView);
        usernameText = findViewById(R.id.userName);

        progressBar = findViewById(R.id.progressBar);

        editProfileBtn.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        Glide.with(this)
                .load(profilePictureUri)
                .error(R.drawable.profile_default)
                .into(profilePicture);

        infoDatabaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                author = snapshot.child("username").getValue(String.class);
                usernameText.setText(author);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        fetchImagesFromStorage(databaseReference);
    }

    public void fetchImagesFromStorage(DatabaseReference databaseReference){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageModels.clear();
                int totalUsers = (int)dataSnapshot.getChildrenCount();

                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()){
                    String imageUrl = imageSnapshot.child("url").getValue(String.class);
                    String author = imageSnapshot.child("author").getValue(String.class);
                    String title = imageSnapshot.child("title").getValue(String.class);

                    infoDatabaseReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            username = snapshot.child("username").getValue(String.class);
                            imageModels.add(new ImageModel(imageUrl, title, username, profilePictureUri.toString()));
                            Utility.checkIfAllItemsLoaded(totalUsers, imageModels, recyclerView, getApplicationContext(), progressBar);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}