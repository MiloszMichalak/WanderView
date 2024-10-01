package com.example.wanderview;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
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
    DatabaseReference databaseReference, infoDatabaseReference;
    String username;

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
                        // TODO Compressor tutaj na zdjecie
                    }
                });

        currentUser = Utility.getCurrentUser();

        storageReference = Utility.getUsersPhotosReference();

        infoDatabaseReference = Utility.getUsersInfoCollectionReference().child(currentUser.getUid());
        infoDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                username = snapshot.child("username").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference = Utility.getUsersPhotosCollectionReference().child(currentUser.getUid());

        imageView = findViewById(R.id.imageView);
        uploadImage = findViewById(R.id.uploadImage);

        imageTitleEdit = findViewById(R.id.imageTitle);

        imageView.setOnClickListener(v -> pickImage.launch(new PickVisualMediaRequest.Builder().
                setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        uploadImage.setOnClickListener(v -> {
            title = imageTitleEdit.getEditText().getText().toString();

            if (photoUri != null){
                storageReference = storageReference.child(currentUser.getUid()).child(fileName);

                storageReference.putFile(photoUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                               String imageUrl = uri.toString();

                                // todo przemyslec dzialanie tego dodawania authora do bazy danych jak w kluczu
                                // todo wyzej jest to samo,  a pozniej pobieranie tego znowu

                                // todo dodawania daty do zdjec
                               Map<String, Object> imageMetadata = new HashMap<>() ;
                               imageMetadata.put("url", imageUrl);
                               imageMetadata.put("author", currentUser.getUid());
                               imageMetadata.put("title", title);
                               imageMetadata.put("date", Timestamp.now().getSeconds());
                               databaseReference.push().setValue(imageMetadata).addOnSuccessListener(unused -> { });
                            });
                            finish();
                        });
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