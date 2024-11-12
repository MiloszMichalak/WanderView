package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ImageView userProfileSettings;
    FirebaseUser currentUser;
    DatabaseReference infoDatabaseReference;
    String username;
    String photoUrl;
    BottomNavigationView bottomNavigationView;
    final Fragment homeFragment = new HomeFragment();
    final Fragment searchFragment = new SearchFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        setupUi();
        initializeFirebaseAuth();
        setupListeners();
        getDataForUser();
    }

    private void initializeFirebaseAuth() {
        currentUser = Utility.getCurrentUser();
        infoDatabaseReference = Utility.getUsersInfoCollectionReference();
    }

    private void setupListeners() {
        userProfileSettings.setOnClickListener(v -> startActivity(new Intent(this, UserProfileActivity.class)));

        fm.beginTransaction().add(R.id.fragmentContainerView, homeFragment, "1").commit();
        fm.beginTransaction().add(R.id.fragmentContainerView, searchFragment, "2").hide(searchFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home){
                fm.beginTransaction().hide(active).show(homeFragment).commit();
                active = homeFragment;
            } else if (item.getItemId() == R.id.search){
                fm.beginTransaction().hide(active).show(searchFragment).commit();
                active = searchFragment;
            }
            return true;
        });
    }

    private void setupUi() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        userProfileSettings = findViewById(R.id.userProfileImage);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void getDataForUser() {
        infoDatabaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                username = snapshot.child("username").getValue(String.class);
                photoUrl = snapshot.child("photoUrl").getValue(String.class);
                Glide.with(getApplicationContext())
                        .load(photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .error(R.drawable.profile_default)
                        .into(userProfileSettings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}