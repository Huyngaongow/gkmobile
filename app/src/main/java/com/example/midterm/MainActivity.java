package com.example.midterm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.midterm.utils.ImageUtils;
import com.example.midterm.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    ImageView ivAvatar;
    TextView tvFullName, tvRole;
    Button btnChangeAvatar, btnManageStudents, btnManageUsers, btnLogout;
    SessionManager sessionManager;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        sessionManager = new SessionManager(this);

        if(!sessionManager.isLoggedIn()){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        View mainView = findViewById(R.id.main);

        if(mainView != null){
            int pL = mainView.getPaddingLeft(), pT = mainView.getPaddingTop(), pR = mainView.getPaddingRight(), pB = mainView.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        pL + sys.left,
                        pT + sys.top,
                        pR + sys.right,
                        pB + sys.bottom
                );
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();
        ivAvatar = findViewById(R.id.ivAvatar);
        tvFullName = findViewById(R.id.tvFullName);
        tvRole = findViewById(R.id.tvRole);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnManageStudents = findViewById(R.id.btnManageStudents);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnLogout = findViewById(R.id.btnLogout);

        btnChangeAvatar.setOnClickListener(v -> openGallery());

        btnManageStudents.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, StudentListActivity.class)));

        if(sessionManager.isAdmin()){
            btnManageUsers.setVisibility(View.VISIBLE);
            btnManageUsers.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UserManagementActivity.class)));
        }
        else{
            btnManageUsers.setVisibility(View.GONE);
        }

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        loadUserProfile();
    }

    private void loadUserProfile(){
        String userId = sessionManager.getUserId();

        if(userId.isEmpty()){
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if(doc.exists()){
                        String fullName = doc.getString("fullName");
                        String role = doc.getString("role");
                        String avatarBase64 = doc.getString("avatarUrl");

                        tvFullName.setText(fullName != null ? fullName : "Người dùng");
                        tvRole.setText("Vai trò: " + (role != null ? role.toUpperCase() : ""));

                        if(avatarBase64 != null && !avatarBase64.trim().isEmpty()){
                            Bitmap bitmap = ImageUtils.base64ToBitmap(avatarBase64);

                            if(bitmap != null){
                                ivAvatar.setImageBitmap(bitmap);
                            }
                            else{
                                ivAvatar.setImageResource(R.drawable.ic_user);
                            }
                        }
                        else{
                            ivAvatar.setImageResource(R.drawable.ic_user);
                        }
                    }
                });
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Uri imageUri = result.getData().getData();

                    if(imageUri != null){
                        try{
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            uploadAndSaveAvatar(bitmap);
                        } catch(Exception e){
                            Toast.makeText(this, "Lỗi chọn ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh đại diện"));
    }

    private void uploadAndSaveAvatar(Bitmap bitmap){
        String base64Image = ImageUtils.bitmapToBase64(bitmap);
        String userId = sessionManager.getUserId();
        db.collection("users").document(userId)
                .update("avatarUrl", base64Image)
                .addOnSuccessListener(aVoid -> {
                    ivAvatar.setImageBitmap(bitmap);
                    Toast.makeText(this, "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}