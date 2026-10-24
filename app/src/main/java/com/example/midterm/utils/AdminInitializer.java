package com.example.midterm.utils;

import android.util.Log;

import com.example.midterm.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminInitializer {
    private static final String TAG = "AdminInitializer";

    public interface OnInitializeListener{
        void onComplete();
    }

    public static void initializeAdminIfNeeded(OnInitializeListener listener){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("username", "admin")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(queryDocumentSnapshots.isEmpty()){
                        String adminId = db.collection("users").document().getId();
                        User admin = new User(
                                adminId,
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
                        db.collection("users").document(adminId)
                                .set(admin)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Default Admin account created successfully!");
                                    if (listener != null){
                                        listener.onComplete();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create default Admin!", e);
                                    if (listener != null){
                                        listener.onComplete();
                                    }
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