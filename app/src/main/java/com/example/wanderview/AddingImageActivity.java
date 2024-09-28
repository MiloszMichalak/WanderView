package com.example.wanderview;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AddingImageActivity extends AppCompatActivity {

    ImageView imageView;
    MaterialButton uploadImage;
    StorageReference storageReference;
    String fileName;
    FirebaseUser currentUser;
    String title;
    TextInputLayout imageTitleEdit;
    Uri photoUri;

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
                        fileName = getPhotoNameFromUri(uri);
                        photoUri = uri;
                    }
                });

        currentUser = Utility.getCurrentUser();

        storageReference = Utility.getUsersPhotosReference();
        DatabaseReference databaseReference  = FirebaseDatabase.getInstance("https://wanderview-8b391-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference().child("UsersPhotos").child(currentUser.getDisplayName());

        imageView = findViewById(R.id.imageView);
        uploadImage = findViewById(R.id.uploadImage);

        imageTitleEdit = findViewById(R.id.imageTitle);

        imageView.setOnClickListener(v -> pickImage.launch(new PickVisualMediaRequest.Builder().
                setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        uploadImage.setOnClickListener(v -> {
            title = imageTitleEdit.getEditText().getText().toString();

            if (!TextUtils.isEmpty(title) && photoUri != null){
                storageReference = storageReference.child(currentUser.getDisplayName()+"/").child(fileName);

                storageReference.putFile(photoUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                               String imageUrl = uri.toString();

                                Map<String, Object> imageMetadata = new HashMap<>() ;
                                imageMetadata.put("url", imageUrl);
                                imageMetadata.put("author", currentUser.getDisplayName());
                                imageMetadata.put("title", title);

                                databaseReference.push().setValue(imageMetadata).addOnSuccessListener(unused -> { });
                            });
                            finish();
                        });
            } else {
                // TODO komunikat ze title nie moze byc pusty
                imageTitleEdit.setError("blad");
            }
        });
    }

    private String getPhotoNameFromUri(Uri uri) {
        String photoName = null;
        ContentResolver contentResolver = getContentResolver();

        String[] projection = { MediaStore
                .Images.Media.DISPLAY_NAME };

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    photoName = cursor.getString(nameIndex);
                }
            } finally {
                cursor.close();
            }
        }

        return photoName;
    }
}