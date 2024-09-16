package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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
    MaterialButton logOutBtn;
    FloatingActionButton addImageBtn;
    FirebaseStorage storage;
    StorageReference storageReference;
    RecyclerView recyclerView;

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

        logOutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
            finish();
        });

        addImageBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AddingImageActivity.class)));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        fetchImagesFromStorage(storageReference);
    }

    // TODO wyswietlanie danych od kazdego uzytkownika sa pod soba a nie w sobie
    public void fetchImagesFromStorage(StorageReference storageReference){
        List<ImageModel> imageModels = new ArrayList<>();
        storageReference.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()){
                item.getMetadata().addOnSuccessListener(metadata -> {
                    String title = metadata.getCustomMetadata("title");
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageModels.add(new ImageModel(uri.toString(), title != null ? title : "Ni ma titla"));
                        if (imageModels.size() == listResult.getItems().size()){
                            ImageAdapter adapter = new ImageAdapter(getApplicationContext(), imageModels);
                            recyclerView.setAdapter(adapter);
                        }
                    });
                });
            }
            for (StorageReference prefix : listResult.getPrefixes()){
                fetchImagesFromStorage(prefix);
            }
        }).addOnFailureListener(e -> {

        });
    }
}