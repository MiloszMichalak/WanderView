package com.example.wanderview;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class Utility {
    public static boolean isValidEmail(CharSequence email){
        return (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static boolean isValidPassword(CharSequence password){
        return (TextUtils.isEmpty(password) || password.length() < 8);
    }

    public static void deleteError(EditText editText, TextInputLayout textInputLayout){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

    }
}
