package com.example.midterm.utils;

import android.util.Log;

import com.example.midterm.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminInitializer {
    private static final String TAG = "AdminInitializer";
    private static final String DEFAULT_ADMIN_ID = "admin_default";

    public interface OnInitializeListener{
        void onComplete();
    }

    public static void initializeAdminIfNeeded(OnInitializeListener listener){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(DEFAULT_ADMIN_ID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(!documentSnapshot.exists()){
                        User admin = new User(
                                DEFAULT_ADMIN_ID,
                                "admin",
                                "admin123",
                                "System Administrator",
                                30,
                                "0900000000",
                                "admin",
                                "normal",
                                "",
                                System.currentTimeMillis()
                        );

                        db.collection("users").document(DEFAULT_ADMIN_ID)
                                .set(admin)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Default Admin account created successfully!");
                                    if (listener != null) listener.onComplete();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create default Admin", e);
                                    if (listener != null) listener.onComplete();
                                });
                    }
                    else{
                        Log.d(TAG, "Admin account already exists!");
                        if(listener != null){
                            listener.onComplete();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking Admin existence", e);
                    if(listener != null){
                        listener.onComplete();
                    }
                });
    }
}