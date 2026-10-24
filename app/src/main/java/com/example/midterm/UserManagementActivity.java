package com.example.midterm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.model.User;
import com.example.midterm.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {
    RecyclerView rvUsers;
    Button btnAddUser;
    FirebaseFirestore db;
    SessionManager sessionManager;
    UserAdapter userAdapter;
    private final List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        sessionManager = new SessionManager(this);

        if(!sessionManager.isAdmin()){
            Toast.makeText(this, "Bạn không có quyền truy cập màn hình này!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_user_management);
        View mainView = findViewById(R.id.main);

        if(mainView != null){
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
        rvUsers = findViewById(R.id.rvUsers);
        btnAddUser = findViewById(R.id.btnAddUser);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, this);
        rvUsers.setAdapter(userAdapter);
        btnAddUser.setOnClickListener(v -> startActivity(new Intent(UserManagementActivity.this, AddEditUserActivity.class)));
        ImageButton btnBack = findViewById(R.id.btnBack);

        if(btnBack != null){
            btnBack.setOnClickListener(v -> finish());
        }

        loadUsersRealtime();
    }

    private void loadUsersRealtime(){
        db.collection("users").addSnapshotListener((value, error) -> {
            if(error != null){
                Toast.makeText(this, "Lỗi tải người dùng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if(value != null){
                userList.clear();

                for(QueryDocumentSnapshot doc : value){
                    User user = doc.toObject(User.class);
                    user.setId(doc.getId());
                    userList.add(user);
                }

                userAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onEdit(User user){
        Intent intent = new Intent(this, AddEditUserActivity.class);
        intent.putExtra("userId", user.getId());
        intent.putExtra("username", user.getUsername());
        intent.putExtra("password", user.getPassword());
        intent.putExtra("fullName", user.getFullName());
        intent.putExtra("age", user.getAge());
        intent.putExtra("phone", user.getPhone());
        intent.putExtra("role", user.getRole());
        intent.putExtra("status", user.getStatus());
        startActivity(intent);
    }

    @Override
    public void onDelete(User user){
        new AlertDialog.Builder(this)
                .setTitle("XÁC NHẬN XÓA")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng @" + user.getUsername() + " không?")
                .setPositiveButton("XÓA", (dialog, which) -> {
                    db.collection("users").document(user.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa người dùng!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    @Override
    public void onViewLogs(User user){
        Intent intent = new Intent(this, LoginHistoryActivity.class);
        intent.putExtra("userId", user.getId());
        intent.putExtra("username", user.getUsername());
        startActivity(intent);
    }
}