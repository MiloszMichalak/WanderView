package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    TextView swapToLogin;
    Button registerBtn;
    TextInputLayout usernameEditText, emailEditText, passwordEditText;
    EditText usernameEdit, emailEdit, passwordEdit;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

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

            boolean isValidData = !Utility.isValidEmail(email) && !Utility.isValidPassword(password) && !isUsernameValid(username);

            if (isUsernameValid(username)) {
                usernameEditText.setError(getString(R.string.invalid_username));
            }

            if (Utility.isValidEmail(email)) {
                emailEditText.setError(getString(R.string.invalid_email));
            }

            if (Utility.isValidPassword(password)) {
                passwordEditText.setError(getString(R.string.invalid_password));
            }

            if (isValidData){
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                currentUser = mAuth.getCurrentUser();

                                currentUser.sendEmailVerification().addOnCompleteListener(this, task1 -> {
                                    if (task1.isSuccessful()){
                                        Toast.makeText(getApplicationContext(), getString(R.string.email_send), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username).build();

                                currentUser.updateProfile(profileUpdates).addOnCompleteListener(task2 -> { });

                                mAuth.signOut();

                                startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                                finish();
                            }
                        });
            }
        });
    }


    public static boolean isUsernameValid(CharSequence username) {
        return (TextUtils.isEmpty(username) || username.length() > 20 || username.length() < 3);
    }
}