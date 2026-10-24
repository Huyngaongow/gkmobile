package com.example.midterm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.midterm.model.LoginLog;
import com.example.midterm.model.User;
import com.example.midterm.utils.AdminInitializer;
import com.example.midterm.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText etUsername, etPassword;
    Button btnLogin;
    FirebaseFirestore db;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        sessionManager = new SessionManager(this);

        if(sessionManager.isLoggedIn()){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        View mainView = findViewById(R.id.main);

        if(mainView != null) {
            int paddingLeft = mainView.getPaddingLeft();
            int paddingTop = mainView.getPaddingTop();
            int paddingRight = mainView.getPaddingRight();
            int paddingBottom = mainView.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        paddingLeft + systemBars.left,
                        paddingTop + systemBars.top,
                        paddingRight + systemBars.right,
                        paddingBottom + systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextInputLayout tilPassword = findViewById(R.id.tilPassword);
        tilPassword.setEndIconOnClickListener(v -> {
            int selection = etPassword.getSelectionEnd();

            if(etPassword.getTransformationMethod() instanceof PasswordTransformationMethod){
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                tilPassword.setEndIconDrawable(R.drawable.ic_hide);
            }
            else{
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                tilPassword.setEndIconDrawable(R.drawable.ic_show);
            }

            if (selection >= 0) {
                etPassword.setSelection(selection);
            }
        });
        AdminInitializer.initializeAdminIfNeeded(null);
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin(){
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if(TextUtils.isEmpty(username)){
            etUsername.setError("Vui lòng nhập tên đăng nhập!");
            etUsername.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(password)){
            etPassword.setError("Vui lòng nhập mật khẩu!");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    btnLogin.setEnabled(true);

                    if(queryDocumentSnapshots.isEmpty()){
                        Toast.makeText(this, "Tên đăng nhập không tồn tại trong hệ thống!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());

                        if(!password.equals(user.getPassword())){
                            Toast.makeText(this, "Mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if("locked".equalsIgnoreCase(user.getStatus())) {
                            Toast.makeText(this, "Tài khoản của bạn đã bị KHÓA! Vui lòng liên hệ Admin.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        sessionManager.saveSession(user);
                        recordLoginLog(user);
                        Toast.makeText(this, "Đăng nhập thành công! Xin chào " + user.getFullName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                        return;
                    }
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Lỗi kết nối máy chủ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void recordLoginLog(User user){
        String logId = db.collection("login_logs").document().getId();
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;
        LoginLog log = new LoginLog(
                logId,
                user.getId(),
                user.getUsername(),
                System.currentTimeMillis(),
                deviceInfo
        );
        db.collection("login_logs").document(logId).set(log);
    }
}