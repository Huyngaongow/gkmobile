package com.example.midterm;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.model.LoginLog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoginHistoryActivity extends AppCompatActivity {
    TextView tvHeaderSubtitle;
    RecyclerView rvLoginLogs;
    ImageButton btnBack;
    FirebaseFirestore db;
    LoginLogAdapter adapter;
    private final List<LoginLog> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_history);
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

        String userId = getIntent().getStringExtra("userId");
        String username = getIntent().getStringExtra("username");
        btnBack = findViewById(R.id.btnBack);
        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);
        rvLoginLogs = findViewById(R.id.rvLoginLogs);

        btnBack.setOnClickListener(v -> finish());

        tvHeaderSubtitle.setText("Tài khoản: @" + (username != null ? username : ""));
        rvLoginLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoginLogAdapter(logList);
        rvLoginLogs.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        if(userId != null && !userId.isEmpty()){
            loadLoginLogs(userId);
        }
    }

    private void loadLoginLogs(String userId){
        db.collection("login_logs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    logList.clear();

                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        LoginLog log = doc.toObject(LoginLog.class);
                        log.setId(doc.getId());
                        logList.add(log);
                    }

                    Collections.sort(logList, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    adapter.notifyDataSetChanged();

                    if(logList.isEmpty()){
                        Toast.makeText(this, "Tài khoản này chưa có lịch sử đăng nhập nào!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải lịch sử: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}