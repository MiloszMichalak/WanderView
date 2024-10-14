package com.example.wanderview.PostModel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wanderview.R;
import com.example.wanderview.Utility;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

public class PostOptionsFragment extends BottomSheetDialogFragment {

    private String postId;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    TextView deletePost;
    FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_options, container, false);

        if (getArguments() != null){
            postId = getArguments().getString("postId");
        }

        currentUser = Utility.getCurrentUser();

        storageReference = Utility.getUsersPhotosReference().child(currentUser.getUid()).child(postId);
        databaseReference = Utility.getUsersPhotosCollectionReference().child(currentUser.getUid()).child(postId);

        deletePost =  view.findViewById(R.id.deletePost);

        deletePost.setOnClickListener(v -> {
            databaseReference.removeValue();
            storageReference.delete().addOnSuccessListener(unused -> {
                Bundle result = new Bundle();
                result.putBoolean("resultBool", true);
                getParentFragmentManager().setFragmentResult("result", result);
                dismiss();
            });
        });

        return view;
    }

    public static PostOptionsFragment newBottomFragment(String postId){
        PostOptionsFragment postOptionsFragment = new PostOptionsFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        postOptionsFragment.setArguments(args);
        return postOptionsFragment;
    }
}