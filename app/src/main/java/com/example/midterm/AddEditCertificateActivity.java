package com.example.midterm;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.midterm.model.Certificate;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditCertificateActivity extends AppCompatActivity {
    ImageButton btnBack;
    TextView tvHeaderTitle;
    TextInputEditText etCertTitle, etCertIssuedBy, etCertIssueDate, etCertExpiryDate, etCertDescription;
    Button btnSaveCert;
    FirebaseFirestore db;
    String studentId = null;
    String certId = null;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_certificate);
        View mainView = findViewById(R.id.main);

        if (mainView != null) {
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

        btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        etCertTitle = findViewById(R.id.etCertTitle);
        etCertIssuedBy = findViewById(R.id.etCertIssuedBy);
        etCertIssueDate = findViewById(R.id.etCertIssueDate);
        etCertExpiryDate = findViewById(R.id.etCertExpiryDate);
        etCertDescription = findViewById(R.id.etCertDescription);
        btnSaveCert = findViewById(R.id.btnSaveCert);

        btnBack.setOnClickListener(v -> finish());

        studentId = getIntent().getStringExtra("studentId");

        if(studentId == null || studentId.isEmpty()){
            Toast.makeText(this, "Không tìm thấy sinh viên!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(getIntent().hasExtra("certId")){
            isEditMode = true;
            certId = getIntent().getStringExtra("certId");
            tvHeaderTitle.setText("Chỉnh sửa Chứng chỉ");
            btnSaveCert.setText("CẬP NHẬT CHỨNG CHỈ");
            etCertTitle.setText(getIntent().getStringExtra("title"));
            etCertIssuedBy.setText(getIntent().getStringExtra("issuedBy"));
            etCertIssueDate.setText(getIntent().getStringExtra("issueDate"));
            etCertExpiryDate.setText(getIntent().getStringExtra("expiryDate"));
            etCertDescription.setText(getIntent().getStringExtra("description"));
        }
        else{
            tvHeaderTitle.setText("Thêm Chứng chỉ Mới");
            btnSaveCert.setText("LƯU CHỨNG CHỈ");
        }

        btnSaveCert.setOnClickListener(v -> saveCertificate());
    }

    private void saveCertificate(){
        String title = etCertTitle.getText() != null ? etCertTitle.getText().toString().trim() : "";
        String issuedBy = etCertIssuedBy.getText() != null ? etCertIssuedBy.getText().toString().trim() : "";
        String issueDate = etCertIssueDate.getText() != null ? etCertIssueDate.getText().toString().trim() : "";
        String expiryDate = etCertExpiryDate.getText() != null ? etCertExpiryDate.getText().toString().trim() : "";
        String description = etCertDescription.getText() != null ? etCertDescription.getText().toString().trim() : "";

        if(TextUtils.isEmpty(title)){
            etCertTitle.setError("Tên chứng chỉ không được để trống!");
            etCertTitle.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(issuedBy)){
            etCertIssuedBy.setError("Nơi cấp không được để trống!");
            etCertIssuedBy.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(issueDate)){
            etCertIssueDate.setError("Ngày cấp không được để trống!");
            etCertIssueDate.requestFocus();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        sdf.setLenient(false);
        Date dateIssued;

        try{
            dateIssued = sdf.parse(issueDate);
        } catch(Exception e){
            etCertIssueDate.setError("Ngày cấp không hợp lệ hoặc không tồn tại (Định dạng: dd/MM/yyyy)!");
            etCertIssueDate.requestFocus();
            return;
        }

        if(!TextUtils.isEmpty(expiryDate)){
            Date dateExpired;
            try{
                dateExpired = sdf.parse(expiryDate);
            } catch(Exception e){
                etCertExpiryDate.setError("Ngày hết hạn không hợp lệ hoặc không tồn tại (Định dạng: dd/MM/yyyy)!");
                etCertExpiryDate.requestFocus();
                return;
            }

            if(!dateExpired.after(dateIssued)){
                etCertExpiryDate.setError("Ngày hết hạn phải xảy ra SAU ngày cấp!");
                etCertExpiryDate.requestFocus();
                return;
            }
        }

        btnSaveCert.setEnabled(false);
        db.collection("students").document(studentId)
                .collection("certificates")
                .whereEqualTo("title", title)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        if(!isEditMode || !doc.getId().equals(certId)){
                            isDuplicate = true;
                            break;
                        }
                    }

                    if(isDuplicate){
                        btnSaveCert.setEnabled(true);
                        etCertTitle.setError("Sinh viên này đã có chứng chỉ tên '" + title + "' rồi!");
                        etCertTitle.requestFocus();
                        return;
                    }

                    if(isEditMode && certId != null){
                        db.collection("students").document(studentId)
                                .collection("certificates").document(certId)
                                .update(
                                        "title", title,
                                        "issuedBy", issuedBy,
                                        "issueDate", issueDate,
                                        "expiryDate", expiryDate,
                                        "description", description
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Cập nhật chứng chỉ thành công!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnSaveCert.setEnabled(true);
                                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                    else{
                        String newCertId = db.collection("students").document(studentId).collection("certificates").document().getId();
                        Certificate newCert = new Certificate(newCertId, studentId, title, issuedBy, issueDate, expiryDate, description);
                        db.collection("students").document(studentId)
                                .collection("certificates").document(newCertId)
                                .set(newCert)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Thêm chứng chỉ thành công!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnSaveCert.setEnabled(true);
                                    Toast.makeText(this, "Lỗi thêm chứng chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    btnSaveCert.setEnabled(true);
                    Toast.makeText(this, "Lỗi kiểm tra trùng chứng chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}