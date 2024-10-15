package com.example.wanderview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class AddingImageActivity extends AppCompatActivity {

    ImageView imageView;
    MaterialButton uploadImage;
    StorageReference storageReference;
    FirebaseUser currentUser;
    String title;
    TextInputLayout imageTitleEdit;
    Uri photoUri;
    DatabaseReference databaseReference;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adding_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityResultLauncher<PickVisualMediaRequest> pickImage =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->{
                    if (uri != null){
                        imageView.setImageURI(uri);
                        photoUri = uri;
                        // TODO Compressor tutaj na zdjecie
                    }
                });

        currentUser = Utility.getCurrentUser();

        storageReference = Utility.getUsersPhotosReference();

        databaseReference = Utility.getUsersPhotosCollectionReference().child(currentUser.getUid()).push();
        key = databaseReference.getKey();

        imageView = findViewById(R.id.imageView);
        uploadImage = findViewById(R.id.uploadImage);

        imageTitleEdit = findViewById(R.id.imageTitle);

        imageView.setOnClickListener(v -> pickImage.launch(new PickVisualMediaRequest.Builder().
                setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        Intent intent = new Intent();

        uploadImage.setOnClickListener(v -> {
            title = imageTitleEdit.getEditText().getText().toString();

            if (photoUri != null){
                storageReference = storageReference.child(currentUser.getUid()).child(key);

                Map<String, Object> imageMetadata = new HashMap<>() ;
                imageMetadata.put("author", currentUser.getUid());
                imageMetadata.put("title", title);
                imageMetadata.put("date", Timestamp.now().getSeconds());
                databaseReference.setValue(imageMetadata);

                storageReference.putFile(photoUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                               String imageUrl = uri.toString();
                               databaseReference.child("url").setValue(imageUrl);
                            });
                        });

                setResult(RESULT_OK, intent);
                intent.putExtra("photoUrl", photoUri.toString());
                intent.putExtra("title", title);
                intent.putExtra("key", key);
                finish();
            }
        });
    }
}