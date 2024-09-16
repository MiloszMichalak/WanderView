package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    MaterialButton logOutBtn;
    FloatingActionButton addImageBtn;
    FirebaseStorage storage;
    StorageReference storageReference;

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

        logOutBtn = findViewById(R.id.logOutBtn);
        addImageBtn = findViewById(R.id.addImageBtn);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        StorageReference storageReference1 = storage.getReference().child("Odanek/");

        ImageView imageView = findViewById(R.id.imageTemp);

        Glide.with(this).load(storageReference1).into(imageView);

        logOutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
            finish();
        });

        addImageBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AddingImageActivity.class)));


    }
}