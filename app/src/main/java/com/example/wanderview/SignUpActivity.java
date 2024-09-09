package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;


public class SignUpActivity extends AppCompatActivity {

    TextView swapToLogin;
    Button registerBtn;
    TextInputLayout usernameEditText, emailEditText, passwordEditText;

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

        registerBtn = findViewById(R.id.registerButton);

        usernameEditText = findViewById(R.id.editTextUsername);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);

        registerBtn.setOnClickListener(v -> {
            String username = usernameEditText.getEditText().getText().toString().trim();
            String email = usernameEditText.getEditText().getText().toString().trim();
            String password = passwordEditText.getEditText().getText().toString().trim();

            if (Utility.isUsernameValid(username)){
                usernameEditText.setError(getString(R.string.invalid_username));
            }

            if (Utility.isValidEmail(email)){
                emailEditText.setError(getString(R.string.invalid_email));
            }

            if (Utility.isValidPassword(password)){
                passwordEditText.setError(getString(R.string.invalid_password));
            }

        });
    }
}