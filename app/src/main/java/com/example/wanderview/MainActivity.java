package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

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
    Fragment homeFragment = new HomeFragment();
    Fragment searchFragment = new SearchFragment();

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        currentUser = Utility.getCurrentUser();

        infoDatabaseReference = Utility.getUsersInfoCollectionReference();

        userProfileSettings = findViewById(R.id.userProfileImage);
        userProfileSettings.setOnClickListener(v -> startActivity(new Intent(this, UserProfileActivity.class)));

        bottomNavigationView = findViewById(R.id.bottomNavigation);

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

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragmentSelected = null;

            if (item.getItemId() == R.id.home){
                fragmentSelected = homeFragment;
            } else if (item.getItemId() == R.id.search){
                fragmentSelected = searchFragment;
            }

            if (fragmentSelected != null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentSelected)
                        .commit();
            }
            return true;
        });
    }
}