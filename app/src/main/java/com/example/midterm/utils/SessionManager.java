package com.example.midterm.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.midterm.model.User;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context){
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveSession(User user){
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_ROLE, user.getRole());
        editor.apply();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId(){
        return pref.getString(KEY_USER_ID, "");
    }

    public String getUsername(){
        return pref.getString(KEY_USERNAME, "");
    }

    public String getFullName(){
        return pref.getString(KEY_FULL_NAME, "");
    }

    public String getRole(){
        return pref.getString(KEY_ROLE, "employee");
    }

    public boolean isAdmin(){
        return "admin".equalsIgnoreCase(getRole());
    }

    public boolean isManagerOrAdmin(){
        String role = getRole();
        return "admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role);
    }

    public void logout(){
        editor.clear();
        editor.apply();
    }
}