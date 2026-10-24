package com.example.midterm;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.midterm.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AddEditUserActivity extends AppCompatActivity {
    ImageButton btnBack;
    TextView tvHeaderTitle;
    TextInputLayout tilUsername, tilPassword;
    TextInputEditText etUsername, etPassword, etFullName, etAge, etPhone;
    Spinner spRole, spStatus;
    Button btnSaveUser;
    FirebaseFirestore db;
    String userId = null;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_user);
        View mainView = findViewById(R.id.main);

        if(mainView != null) {
            int pL = mainView.getPaddingLeft(), pT = mainView.getPaddingTop(), pR = mainView.getPaddingRight(), pB = mainView.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        pL + sys.left,
                        pT + sys.top,
                        pR + sys.right,
                        pB + sys.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();
        btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        spRole = findViewById(R.id.spRole);
        spStatus = findViewById(R.id.spStatus);
        btnSaveUser = findViewById(R.id.btnSaveUser);

        btnBack.setOnClickListener(v -> finish());

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

            if(selection >= 0){
                etPassword.setSelection(selection);
            }
        });

        String[] roles = {"manager", "employee", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, roles);
        roleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spRole.setAdapter(roleAdapter);

        String[] statuses = {"normal", "locked"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, statuses);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spStatus.setAdapter(statusAdapter);

        if(getIntent().hasExtra("userId")){
            isEditMode = true;
            userId = getIntent().getStringExtra("userId");
            tvHeaderTitle.setText("Chỉnh sửa Người dùng");
            btnSaveUser.setText("CẬP NHẬT THÔNG TIN");
            etUsername.setText(getIntent().getStringExtra("username"));
            etUsername.setEnabled(false);
            etPassword.setText(getIntent().getStringExtra("password"));
            etFullName.setText(getIntent().getStringExtra("fullName"));
            etAge.setText(String.valueOf(getIntent().getIntExtra("age", 20)));
            etPhone.setText(getIntent().getStringExtra("phone"));
            String role = getIntent().getStringExtra("role");

            if("manager".equalsIgnoreCase(role)){
                spRole.setSelection(0);
            }
            else if("employee".equalsIgnoreCase(role)){
                spRole.setSelection(1);
            }
            else{
                spRole.setSelection(2);
            }

            String status = getIntent().getStringExtra("status");

            if("locked".equalsIgnoreCase(status)){
                spStatus.setSelection(1);
            }
            else{
                spStatus.setSelection(0);
            }
        }
        else{
            tvHeaderTitle.setText("Thêm Người dùng Mới");
            btnSaveUser.setText("LƯU NGƯỜI DÙNG");
        }

        btnSaveUser.setOnClickListener(v -> saveUser());
    }

    private void saveUser(){
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String role = spRole.getSelectedItem().toString();
        String status = spStatus.getSelectedItem().toString();

        if(TextUtils.isEmpty(username)){
            etUsername.setError("Vui lòng nhập tên đăng nhập!");
            etUsername.requestFocus();
            return;
        }

        if(username.contains(" ") || username.length() < 2){
            etUsername.setError("Tên đăng nhập phải viết liền không dấu, từ 2 ký tự trở lên!");
            etUsername.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(password)){
            etPassword.setError("Vui lòng nhập mật khẩu!");
            etPassword.requestFocus();
            return;
        }

        if(password.length() < 6){
            etPassword.setError("Mật khẩu phải từ 6 ký tự trở lên!");
            etPassword.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(fullName)){
            etFullName.setError("Vui lòng nhập họ và tên!");
            etFullName.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(ageStr)){
            etAge.setError("Vui lòng nhập tuổi!");
            etAge.requestFocus();
            return;
        }

        int age;
        try{
            age = Integer.parseInt(ageStr);

            if(age < 18 || age > 100){
                etAge.setError("Tuổi người dùng phải từ 18 đến 100!");
                etAge.requestFocus();
                return;
            }
        } catch (Exception e){
            etAge.setError("Tuổi không hợp lệ!");
            etAge.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(phone)){
            etPhone.setError("Vui lòng nhập số điện thoại!");
            etPhone.requestFocus();
            return;
        }

        if(!phone.matches("^0[0-9]{9}$")){
            etPhone.setError("Số điện thoại phải gồm 10 chữ số bắt đầu bằng 0!");
            etPhone.requestFocus();
            return;
        }

        btnSaveUser.setEnabled(false);

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        if(!isEditMode || !doc.getId().equals(userId)){
                            isDuplicate = true;
                            break;
                        }
                    }

                    if(isDuplicate){
                        btnSaveUser.setEnabled(true);
                        etUsername.setError("Tên đăng nhập '" + username + "' đã tồn tại ở tài khoản khác!");
                        etUsername.requestFocus();
                        return;
                    }

                    if(isEditMode && userId != null){
                        db.collection("users").document(userId)
                                .update(
                                        "password", password,
                                        "fullName", fullName,
                                        "age", age,
                                        "phone", phone,
                                        "role", role,
                                        "status", status
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Cập nhật tài khoản thành công!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnSaveUser.setEnabled(true);
                                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                    else{
                        String newId = db.collection("users").document().getId();
                        User newUser = new User(newId, username, password, fullName, age, phone, role, status, "", System.currentTimeMillis());

                        db.collection("users").document(newId).set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Thêm người dùng thành công!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnSaveUser.setEnabled(true);
                                    Toast.makeText(this, "Lỗi thêm người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    btnSaveUser.setEnabled(true);
                    Toast.makeText(this, "Lỗi kiểm tra trùng tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}