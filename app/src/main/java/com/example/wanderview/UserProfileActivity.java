package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
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
    String profilePictureUri;
    List<ImageModel> imageModels = new ArrayList<>();
    ImageButton userOption;
    SwipeRefreshLayout swipeRefreshLayout;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, true));

        Intent intent = getIntent();
        editProfileBtn = findViewById(R.id.editProfileBtn);

        userOption = findViewById(R.id.userOption);

        if (intent.getStringExtra("Author") != null){
            author = intent.getStringExtra("Author");
            editProfileBtn.setVisibility(View.INVISIBLE);
            userOption.setVisibility(View.INVISIBLE);
        } else {
            author = currentUser.getUid();
        }

        infoDatabaseReference = Utility.getUsersInfoCollectionReference();
        databaseReference = Utility.getUsersPhotosCollectionReference().child(author);

        usernameText = findViewById(R.id.userName);

        infoDatabaseReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                author = snapshot.child("username").getValue(String.class);

                profilePictureUri = snapshot.child("photoUrl").getValue(String.class);
                Glide.with(getApplicationContext())
                        .load(profilePictureUri)
                        .error(R.drawable.profile_default)
                        .into(profilePicture);

                usernameText.setText(author);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profilePicture = findViewById(R.id.imageView);

        progressBar = findViewById(R.id.progressBar);

        editProfileBtn.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        userOption.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.option1) {
                    FirebaseAuth.getInstance().signOut();
                    Intent logOut = new Intent(this, LogInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(logOut);
                    finish();
                }
                return false;
            });
        });

        swipeRefreshLayout = findViewById(R.id.main);

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorSecondary);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchImagesFromStorage(databaseReference);
            swipeRefreshLayout.setRefreshing(false);
        });

        fetchImagesFromStorage(databaseReference);
    }

    // todo dodac ze jak user usuwa posta to odswieza mu adapter

    public void fetchImagesFromStorage(DatabaseReference databaseReference){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageModels.clear();

                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()){
                    String imageUrl = imageSnapshot.child("url").getValue(String.class);
                    String author = imageSnapshot.child("author").getValue(String.class);
                    String title = imageSnapshot.child("title").getValue(String.class);
                    Long timestamp = imageSnapshot.child("date").getValue(Long.class);
                    String key = imageSnapshot.getKey();

                    infoDatabaseReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            username = snapshot.child("username").getValue(String.class);
                            int likes = imageSnapshot.child("likeAmmount").getValue(Integer.class);

                            imageModels.add(new ImageModel(imageUrl,
                                    title,
                                    username,
                                    profilePictureUri,
                                    author,
                                    key,
                                    timestamp,
                                    likes));
                            Utility.allItemsLoaded(imageModels, recyclerView, getApplicationContext(), progressBar, false, getSupportFragmentManager());
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