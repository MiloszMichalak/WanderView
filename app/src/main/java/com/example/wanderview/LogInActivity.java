package com.example.wanderview;

import android.content.Intent;
import android.os.Bundle;
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

public class LogInActivity extends AppCompatActivity {

    TextView swapToSignUp;
    TextInputLayout editTextEmail, editTextPassword;
    EditText emailEdit, passwordEdit;
    Button logInBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        swapToSignUp = findViewById(R.id.swapToSignUp);

        swapToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            finish();
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        logInBtn = findViewById(R.id.logInButton);

        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);

        Utility.deleteError(emailEdit, editTextEmail);
        Utility.deleteError(passwordEdit, editTextPassword);

        logInBtn.setOnClickListener(view -> {
            String email = editTextEmail.getEditText().getText().toString().trim();
            String password = editTextPassword.getEditText().getText().toString().trim();

            boolean isValidData;

            isValidData = !Utility.isValidEmail(email) && !Utility.isValidPassword(password);

            if (Utility.isValidEmail(email)) {
                editTextEmail.setError(getString(R.string.invalid_email));
            }

            if (Utility.isValidPassword(password)) {
                editTextPassword.setError(getString(R.string.invalid_password));
            }

            if (isValidData){
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (!currentUser.isEmailVerified()){
                            Toast.makeText(getApplicationContext(), R.string.must_verify, Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        } else {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    } else {
                        editTextEmail.setError("\t");
                        editTextPassword.setError(getString(R.string.invalid_data));
                    }
                });
            }
        });


    }
}