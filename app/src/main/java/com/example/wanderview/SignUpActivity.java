package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class SignUpActivity extends AppCompatActivity {

    TextView swapToLogin;
    Button registerBtn;
    EditText usernameEditText, emailEditText, passwordEditText;

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
            String username = usernameEditText.getText().toString();
            String email = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty()){

                usernameEditText.setError(getString(R.string.invalid_username));
            }

            if (isValidEmail(email)){
                emailEditText.setError(getString(R.string.invalid_email));
            }

            if (isValidPassword(password)){
                passwordEditText.setError(getString(R.string.invalid_password));
            }

        });
    }

    public static boolean isValidEmail(CharSequence email){
        return (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static boolean isValidPassword(CharSequence password){
        return (TextUtils.isEmpty(password) || password.length() < 8);
    }
}