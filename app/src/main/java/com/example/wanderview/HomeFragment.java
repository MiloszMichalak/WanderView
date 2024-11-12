package com.example.wanderview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wanderview.PostModel.ImageAdapter;
import com.example.wanderview.PostModel.ImageModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    FirebaseUser currentUser;
    FloatingActionButton addImageBtn;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<ImageModel> imageModels = new ArrayList<>();
    DatabaseReference databaseReference, infoDatabaseReference;
    SwipeRefreshLayout swipeRefreshLayout;
    LifecycleOwner lifecycleOwner;
    View view;
    ImageAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
        if (o.getResultCode() == Activity.RESULT_OK) {
            Intent data = o.getData();
            if (data != null) {
                infoDatabaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        imageModels.add(0,
                                new ImageModel(
                                        data.getStringExtra("photoUrl"),
                                        data.getStringExtra("title"),
                                        snapshot.child("username").getValue(String.class),
                                        snapshot.child("photoUrl").getValue(String.class),
                                        currentUser.getUid(),
                                        data.getStringExtra("key"),
                                        Timestamp.now().getSeconds(),
                                        0,
                                        false,
                                        0,
                                        data.getStringExtra("type")
                                ));
                        recyclerView.getAdapter().notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    });

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        lifecycleOwner = this;

        setupUi();
        initializeFirebaseAuth();

        addImageBtn.setOnClickListener(v ->
                resultLauncher.launch(new Intent(view.getContext(), AddingImageActivity.class)));

        setupRecyclerView();
        setupSwipeRefresh();

        return view;
    }

    private void initializeFirebaseAuth() {
        currentUser = Utility.getCurrentUser();
        databaseReference = Utility.getUsersPhotosCollectionReference();
        infoDatabaseReference = Utility.getUsersInfoCollectionReference();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorSecondary);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);

            adapter.resetAll();
            fetchItemsFromStorage(databaseReference);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        fetchItemsFromStorage(databaseReference);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                swipeRefreshLayout.setEnabled(!recyclerView.canScrollVertically(-1));
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        if (i < firstVisible || i > lastVisible){
                            adapter.pauseVideoAtPosition(i);
                        } else {
                            adapter.resumeVideoAtPosition(i);
                        }
                    }
                }
            }
        });
    }

    private void setupUi() {
        addImageBtn = view.findViewById(R.id.addImageBtn);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
    }

    public void fetchItemsFromStorage(DatabaseReference databaseReference) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageModels.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    for (DataSnapshot imageSnapshot : userSnapshot.getChildren()) {
                        String author = imageSnapshot.child("author").getValue(String.class);
                        String imageUrl = imageSnapshot.child("url").getValue(String.class);
                        String title = imageSnapshot.child("title").getValue(String.class);
                        String type = imageSnapshot.child("type").getValue(String.class);
                        Long date = imageSnapshot.child("date").getValue(Long.class);
                        String key = imageSnapshot.getKey();

                        if (!author.equals(currentUser.getUid())) {
                            boolean likedByCurrentUser = imageSnapshot.child("likes").child(currentUser.getUid()).exists();

                            infoDatabaseReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    long likes = imageSnapshot.child("likes").getChildrenCount();
                                    long commentsAmount = imageSnapshot.child("comments").getChildrenCount();

                                    imageModels.add(new ImageModel(
                                            imageUrl,
                                            title,
                                            snapshot.child("username").getValue(String.class),
                                            snapshot.child("photoUrl").getValue(String.class),
                                            author,
                                            key,
                                            date,
                                            likes,
                                            likedByCurrentUser,
                                            commentsAmount,
                                            type
                                    ));
                                    if (imageModels.size() == dataSnapshot.getChildrenCount()) {
                                        Collections.shuffle(imageModels);
                                        adapter = Utility.allImagesLoaded(imageModels, recyclerView, getContext(), progressBar, true, getActivity().getSupportFragmentManager(), lifecycleOwner);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }
}