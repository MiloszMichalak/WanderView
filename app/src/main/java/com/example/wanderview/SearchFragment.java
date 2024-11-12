package com.example.wanderview;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wanderview.UserListModel.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    TextInputEditText editText;
    DatabaseReference infoDatabaseReference;
    List<UserModel> users = new ArrayList<>();
    RecyclerView recyclerView;
    TextView noUsers;
    Runnable searchRunnable;
    View view;
    Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);

        setupUi();
        infoDatabaseReference = Utility.getUsersInfoCollectionReference();
        handler = new Handler();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleTextChange(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void handleTextChange(String s) {
        String text = s.toLowerCase();
        recyclerView.setAdapter(null);
        noUsers.setVisibility(View.INVISIBLE);

        if (searchRunnable != null){
            handler.removeCallbacks(searchRunnable);
        }

        if (!text.isEmpty()){
            searchRunnable = () -> searchUsersInDatabase(text);
            handler.postDelayed(searchRunnable, 200);
        }
    }

    private void searchUsersInDatabase(String text) {
        infoDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String username = snapshot.child("username").getValue(String.class);
                    if (username.toLowerCase().startsWith(text)){
                        users.add(new UserModel(
                                snapshot.getKey(),
                                username,
                                snapshot.child("photoUrl").getValue(String.class)));
                    }
                }

                updateUserList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        handler.postDelayed(searchRunnable, 200);
    }

    private void updateUserList() {
        if (users.isEmpty()){
            recyclerView.setAdapter(null);
            noUsers.setVisibility(View.VISIBLE);
        } else {
            noUsers.setVisibility(View.INVISIBLE);
            Utility.allUsersLoaded(users, recyclerView, view.getContext());
        }
    }

    private void setupUi() {
        editText = view.findViewById(R.id.contentEditText);
        recyclerView = view.findViewById(R.id.recyclerView);
        noUsers = view.findViewById(R.id.noUsers);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
    }
}