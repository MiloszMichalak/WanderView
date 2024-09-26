package com.example.wanderview;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class AddingImageActivity extends AppCompatActivity {

    ImageView imageView;
    MaterialButton uploadImage;
    FirebaseStorage storage;
    StorageReference storageReference;
    String fileName;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String title;
    TextInputLayout imageTitleEdit;

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
                    }
                });


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("UsersPhotos/");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        imageView = findViewById(R.id.imageView);
        uploadImage = findViewById(R.id.uploadImage);

        imageTitleEdit = findViewById(R.id.imageTitle);

        imageView.setOnClickListener(v -> pickImage.launch(new PickVisualMediaRequest.Builder().
                setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        uploadImage.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            title = imageTitleEdit.getEditText().getText().toString();

            // TODO jak nie ma imageview to po prostu ze nie ma a nie ze wylacza activity
            if (data.length > 0 && !TextUtils.isEmpty(title)){
                storageReference = storageReference.child(currentUser.getDisplayName()+"/").child(fileName);

                storageReference.putBytes(data)
                        .addOnFailureListener(exception -> { })
                        .addOnSuccessListener(taskSnapshot -> {
                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setCustomMetadata("author", currentUser.getDisplayName())
                                    .setCustomMetadata("title", title)
                                    .build();
                            storageReference.updateMetadata(metadata)
                                    .addOnSuccessListener(storageMetadata -> { });
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