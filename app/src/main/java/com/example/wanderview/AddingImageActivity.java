package com.example.wanderview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AddingImageActivity extends AppCompatActivity {

    private ImageView imageView;
    private MaterialButton uploadImage;
    private StorageReference storageReference;
    private FirebaseUser currentUser;
    private String title;
    private TextInputLayout imageTitleEdit;
    private Uri photoUri;
    private DatabaseReference databaseReference;
    private String key, type;
    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;
    private boolean isPlaying;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImage =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    String mimeType = getContentResolver().getType(uri);
                    if (mimeType != null && mimeType.startsWith("image")) {
                        type = ifGetImage(uri);
                    } else if (mimeType != null && mimeType.startsWith("video")) {
                        type = ifGetVideo(uri);
                    } else {
                        type = "unknown";
                    }
                    photoUri = uri;

                    // TODO Compressor tutaj na zdjecie(juz nie tylko)
                }
            });

    private String ifGetVideo(Uri uri) {
        imageView.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);

        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

        playerView.setPlayer(exoPlayer);

        MediaItem mediaItem = MediaItem.fromUri(uri);

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        return "video";
    }

    private String ifGetImage(Uri uri) {
        playerView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageURI(uri);

        return "image";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adding_image);

        launchPickImage();
        setupUi();
        setupDatabase();

        imageView.setOnClickListener(v -> launchPickImage());

        Intent intent = new Intent();

        uploadImage.setOnClickListener(v -> {
            title = imageTitleEdit.getEditText().getText().toString();

            if (photoUri != null) {
                addImage();

                setResult(RESULT_OK, intent);
                intent.putExtra("photoUrl", photoUri.toString());
                intent.putExtra("title", title);
                intent.putExtra("key", key);
                intent.putExtra("type", type);
                finish();
            }
        });
    }

    private void addImage() {
        addImageToRealTime();
        addImageToStorage();
    }

    private void addImageToRealTime() {
        Map<String, Object> imageMetadata = new HashMap<>();
        imageMetadata.put("author", currentUser.getUid());
        imageMetadata.put("title", title);
        imageMetadata.put("date", Timestamp.now().getSeconds());
        imageMetadata.put("type", type);
        databaseReference.setValue(imageMetadata);
    }

    private void addImageToStorage() {
        storageReference.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        databaseReference.child("url").setValue(uri.toString());
                    });
                });
    }

    private void setupUi() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);
        playerView = findViewById(R.id.playerView);
        uploadImage = findViewById(R.id.uploadImage);
        imageTitleEdit = findViewById(R.id.imageTitle);
    }

    private void launchPickImage() {
        pickImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private void setupDatabase() {
        currentUser = Utility.getCurrentUser();
        databaseReference = Utility.getUsersPhotosCollectionReference().child(currentUser.getUid()).push();
        key = databaseReference.getKey();
        storageReference = Utility.getUsersPhotosReference().child(currentUser.getUid()).child(key);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null){
            exoPlayer.release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayer != null){
            isPlaying = exoPlayer.isPlaying();
            exoPlayer.stop();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isPlaying && exoPlayer != null) {
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }
}