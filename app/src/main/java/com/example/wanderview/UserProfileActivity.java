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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    ImageView profilePicture;
    FirebaseUser currentUser;
    TextView username;
    StorageReference userProfileImageReference;
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    MaterialButton editProfileBtn;
    ProgressBar progressBar;
    String author;
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

        if (intent.getStringExtra("Author") != null){
            author = intent.getStringExtra("Author");
            editProfileBtn.setVisibility(View.INVISIBLE);
        } else {
            author = currentUser.getDisplayName();
        }

        if (intent.getStringExtra("AuthorProfileImage") != null){
            profilePictureUri = Uri.parse(intent.getStringExtra("AuthorProfileImage"));
        }

        userProfileImageReference = Utility.getUsersProfilePhotosReference();
        databaseReference = FirebaseDatabase.getInstance("https://wanderview-8b391-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference().child("UsersPhotos").child(author  );

        profilePicture = findViewById(R.id.imageView);
        username = findViewById(R.id.userName);

        progressBar = findViewById(R.id.progressBar);

        editProfileBtn.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        userProfileImageReference.child(author).getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .error(R.drawable.profile_default)
                    .into(profilePicture);
            if (profilePictureUri == null){
                profilePictureUri = uri;
            }
        });

        username.setText(author);

        fetchImagesFromStorage(databaseReference);
    }

    // TODO inny kod na system inny

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

                    imageModels.add(new ImageModel(imageUrl, author, title, profilePictureUri));
                    Utility.checkIfAllItemsLoaded(totalUsers, imageModels, recyclerView, getApplicationContext(), progressBar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}