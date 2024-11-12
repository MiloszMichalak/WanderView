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
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.wanderview.PostModel.ImageAdapter;
import com.example.wanderview.PostModel.ImageModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    ImageView profilePicture;
    FirebaseUser currentUser;
    TextView usernameText, noPosts;
    DatabaseReference databaseReference, infoDatabaseReference;
    RecyclerView recyclerView;
    MaterialButton editProfileBtn;
    ProgressBar progressBar;
    String author;
    String profilePictureUri;
    List<ImageModel> imageModels = new ArrayList<>();
    ImageButton userOption;
    SwipeRefreshLayout swipeRefreshLayout;
    Toolbar toolbar;
    Intent intent;
    LifecycleOwner lifecycleOwner;
    ImageAdapter adapter;

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null){
            adapter.pauseAll();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null){
            adapter.resumeAll();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        setupUi();
        setupToolbar();

        lifecycleOwner = this;
        intent = getIntent();

        currentUser = Utility.getCurrentUser();
        getDataFromIntent();

        initializeFirebase();

        getUserInfo();
        setupListeners();
        setupSwipeToRefresh();
        fetchItemsFromStorage(databaseReference);
    }

    private void initializeFirebase() {
        infoDatabaseReference = Utility.getUsersInfoCollectionReference();
        databaseReference = Utility.getUsersPhotosCollectionReference().child(author);
    }

    private void setupListeners() {
        editProfileBtn.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        userOption.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.option1) {
                    FirebaseAuth.getInstance().signOut();
                    Intent logout = new Intent(this, LogInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(logout);
                    finish();
                }
                return false;
            });
        });
    }

    private void getUserInfo() {
        infoDatabaseReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                author = snapshot.child("username").getValue(String.class);
                profilePictureUri = snapshot.child("photoUrl").getValue(String.class);

                Glide.with(getApplicationContext())
                        .load(profilePictureUri)
                        .error(R.drawable.profile_default)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                        .into(profilePicture);

                getSupportActionBar().setTitle(author);
                usernameText.setText(author);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDataFromIntent() {
        author = intent.getStringExtra("Author");

        if (author != null && !author.equals(currentUser.getUid())){
            author = intent.getStringExtra("Author");
            editProfileBtn.setVisibility(View.INVISIBLE);
            userOption.setVisibility(View.INVISIBLE);
        } else {
            author = currentUser.getUid();
        }
    }

    private void setupUi() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        editProfileBtn = findViewById(R.id.edit_profile_btn);
        userOption = findViewById(R.id.user_option);
        usernameText = findViewById(R.id.user_name);
        recyclerView = findViewById(R.id.imageList);
        profilePicture = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        noPosts = findViewById(R.id.no_posts);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorSecondary);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            adapter.resetAll();
            fetchItemsFromStorage(databaseReference);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void fetchItemsFromStorage(DatabaseReference databaseReference){
        databaseReference.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long posts = dataSnapshot.getChildrenCount();
                if (posts == 0){
                    noPosts.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                imageModels.clear();

                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()){
                    String imageUrl = imageSnapshot.child("url").getValue(String.class);
                    String author = imageSnapshot.child("author").getValue(String.class);
                    String title = imageSnapshot.child("title").getValue(String.class);
                    String type = imageSnapshot.child("type").getValue(String.class);
                    Long timestamp = imageSnapshot.child("date").getValue(Long.class);
                    String key = imageSnapshot.getKey();
                    boolean likedByCurrentUser = imageSnapshot.child("likes").child(currentUser.getUid()).exists();
                    Long commentsAmount = imageSnapshot.child("comments").getChildrenCount();

                    infoDatabaseReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String username = snapshot.child("username").getValue(String.class);
                            long likes = imageSnapshot.child("likes").getChildrenCount();

                            imageModels.add(new ImageModel(imageUrl,
                                    title,
                                    username,
                                    profilePictureUri,
                                    author,
                                    key,
                                    timestamp,
                                    likes,
                                    likedByCurrentUser,
                                    commentsAmount,
                                    type));

                            if (imageModels.size() == dataSnapshot.getChildrenCount()){
                                Collections.reverse(imageModels);
                                adapter = Utility.allImagesLoaded(imageModels, recyclerView, getApplicationContext(), progressBar, false, getSupportFragmentManager(), lifecycleOwner);
                            }
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