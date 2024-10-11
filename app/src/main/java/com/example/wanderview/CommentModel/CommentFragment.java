package com.example.wanderview.CommentModel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wanderview.R;
import com.example.wanderview.Utility;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentFragment extends BottomSheetDialogFragment {

    String postId;
    String userId;
    DatabaseReference databaseReference, infoDatabaseReference;
    RecyclerView recyclerView;
    List<CommentModel> commentsList = new ArrayList<>();
    ImageView addCommentBtn;
    FirebaseUser currentUser;
    TextInputLayout commentContent;
    ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        currentUser = Utility.getCurrentUser();

        if (getArguments() != null){
            postId = getArguments().getString("postId");
            userId = getArguments().getString("userId");
        }

        databaseReference = Utility.getUsersPhotosCollectionReference().child(userId).child(postId).child("comments");
        infoDatabaseReference = Utility.getUsersInfoCollectionReference();

        imageView = view.findViewById(R.id.userProfileImage);

        infoDatabaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Glide.with(view.getContext())
                        .load(snapshot.child("photoUrl").getValue(String.class))
                        .circleCrop()
                        .into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        addCommentBtn = view.findViewById(R.id.addCommentBtn);

        commentContent = view.findViewById(R.id.commentContentEditText);

        addCommentBtn.setOnClickListener(v -> {
            // todo zabezpieczenia odnosnie pustego edit textta - wyswietla sie ikona - nie wyswietla
            String content = commentContent.getEditText().getText().toString();
            Map<String, Object> commentData = new HashMap<>();

            commentData.put("author", currentUser.getUid());
            commentData.put("timestamp", Timestamp.now().getSeconds());
            commentData.put("commentContent", content);

            databaseReference.push().setValue(commentData);

            commentContent.getEditText().setText(null);
        });

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsList.clear();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()){

                    String author = commentSnapshot.child("author").getValue(String.class);
                    String commentContent = commentSnapshot.child("commentContent").getValue(String.class);
                    long timestamp = commentSnapshot.child("timestamp").getValue(Long.class);
                    String key = commentSnapshot.getKey();
                    long likes = commentSnapshot.child("likes").getChildrenCount();
                    boolean isUserLiked = commentSnapshot.child("likes").child(currentUser.getUid()).exists();

                    infoDatabaseReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String username = snapshot.child("username").getValue(String.class);
                            String profileImageUrl = snapshot.child("photoUrl").getValue(String.class);
                            commentsList.add(new CommentModel(profileImageUrl,
                                    username,
                                    timestamp,
                                    commentContent,
                                    likes,
                                    isUserLiked,
                                    author,
                                    key,
                                    postId,
                                    userId
                            ));
                            Utility.allCommentsLoaded(commentsList, recyclerView, getContext());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                // TODO system zapisywania - automatycznie wyswietl nowy
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    public static CommentFragment newCommentFragment(String postId, String userId){
        CommentFragment commentFragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putString("userId", userId);
        commentFragment.setArguments(args);
        return commentFragment;
    }
}