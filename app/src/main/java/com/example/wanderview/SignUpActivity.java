package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    TextView swapToLogin;
    Button registerBtn;
    TextInputLayout usernameEditText, emailEditText, passwordEditText;
    EditText usernameEdit, emailEdit, passwordEdit;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference infoDatabaseReference;
    boolean isValidData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        swapToLogin = findViewById(R.id.swapToLogin);
        swapToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LogInActivity.class));
            finish();
        });

        mAuth = FirebaseAuth.getInstance();
        infoDatabaseReference = Utility.getUsersInfoCollectionReference();

        registerBtn = findViewById(R.id.registerButton);

        usernameEditText = findViewById(R.id.editTextUsername);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);

        usernameEdit = findViewById(R.id.usernameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);

        Utility.deleteError(usernameEdit, usernameEditText);

        Utility.deleteError(emailEdit, emailEditText);

        Utility.deleteError(passwordEdit, passwordEditText);

        registerBtn.setOnClickListener(v -> {
            String username = usernameEditText.getEditText().getText().toString().trim();
            String email = emailEditText.getEditText().getText().toString().trim();
            String password = passwordEditText.getEditText().getText().toString().trim();

            isValidData = !Utility.isValidEmail(email) && !Utility.isValidPassword(password) && !Utility.isUsernameValid(username);

            if (Utility.isUsernameValid(username)) {
                usernameEditText.setError(getString(R.string.invalid_username));
            }

            if (Utility.isValidEmail(email)) {
                emailEditText.setError(getString(R.string.invalid_email));
            }

            if (Utility.isValidPassword(password)) {
                passwordEditText.setError(getString(R.string.invalid_password));
            }

            infoDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if (snapshot.child("username").getValue(String.class).toLowerCase().equals(username.toLowerCase())){
                            isValidData = false;
                            usernameEditText.setError(getString(R.string.taken_username));
                            break;
                        }
                    }
                    if (isValidData){
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(SignUpActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        currentUser = mAuth.getCurrentUser();
                                        currentUser.sendEmailVerification().addOnCompleteListener(SignUpActivity.this, task1 -> {
                                            if (task1.isSuccessful()){
                                                Toast.makeText(getApplicationContext(), getString(R.string.email_send), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        infoDatabaseReference.child(currentUser.getUid()).child("username").setValue(username);

                                        mAuth.signOut();

                                        startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                                        finish();
                                    } else {
                                        emailEditText.setError(getString(R.string.taken_email));
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }
}