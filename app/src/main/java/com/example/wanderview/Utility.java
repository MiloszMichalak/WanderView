package com.example.wanderview;

import android.text.TextUtils;
import android.util.Patterns;

public class Utility {
    public static boolean isValidEmail(CharSequence email){
        return (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static boolean isValidPassword(CharSequence password){
        return (TextUtils.isEmpty(password) || password.length() < 8);
    }

    public static boolean isUsernameValid(CharSequence username) {
        return (TextUtils.isEmpty(username) || username.length() > 20 || username.length() < 3);
    }
}
