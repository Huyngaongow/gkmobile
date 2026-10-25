package com.example.midterm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.model.Certificate;
import com.example.midterm.utils.CsvUtils;
import com.example.midterm.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class StudentDetailActivity extends AppCompatActivity implements CertificateAdapter.OnCertificateActionListener {
    ImageButton btnBack;
    TextView tvDetailStudentCode, tvDetailFullName, tvDetailClassAndAge, tvDetailGpa, tvDetailEmail, tvDetailPhone;
    Button btnAddCertificate, btnExportCertCsv, btnImportCertCsv;
    RecyclerView rvCertificates;
    FirebaseFirestore db;
    SessionManager sessionManager;
    CertificateAdapter adapter;
    private final List<Certificate> certificateList = new ArrayList<>();
    String studentId;
    LinearLayout layoutCertImportExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_detail);
        View mainView = findViewById(R.id.main);

        if(mainView != null){
            int pL = mainView.getPaddingLeft(), pT = mainView.getPaddingTop(), pR = mainView.getPaddingRight(), pB = mainView.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(pL + sys.left, pT + sys.top, pR + sys.right, pB + sys.bottom);
                return insets;
            });
        }

        sessionManager = new SessionManager(this);
        db = FirebaseFirestore.getInstance();
        btnBack = findViewById(R.id.btnBack);
        tvDetailStudentCode = findViewById(R.id.tvDetailStudentCode);
        tvDetailFullName = findViewById(R.id.tvDetailFullName);
        tvDetailClassAndAge = findViewById(R.id.tvDetailClassAndAge);
        tvDetailGpa = findViewById(R.id.tvDetailGpa);
        tvDetailEmail = findViewById(R.id.tvDetailEmail);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        btnAddCertificate = findViewById(R.id.btnAddCertificate);
        rvCertificates = findViewById(R.id.rvCertificates);

        btnBack.setOnClickListener(v -> finish());

        studentId = getIntent().getStringExtra("studentId");

        if(studentId == null || studentId.isEmpty()){
            Toast.makeText(this, "Không tìm thấy sinh viên!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDetailStudentCode.setText("MÃ SV: " + getIntent().getStringExtra("studentCode"));
        tvDetailFullName.setText(getIntent().getStringExtra("fullName"));
        tvDetailClassAndAge.setText("Lớp: " + getIntent().getStringExtra("classId") + " • " + getIntent().getIntExtra("age", 20) + " tuổi (" + getIntent().getStringExtra("gender") + ")");
        tvDetailGpa.setText(String.format("Điểm trung bình (GPA): %.1f", getIntent().getDoubleExtra("gpa", 0.0)));
        tvDetailEmail.setText("Email: " + getIntent().getStringExtra("email"));
        tvDetailPhone.setText("Số điện thoại: " + getIntent().getStringExtra("phone"));

        boolean isEditable = sessionManager.isManagerOrAdmin();

        if(!isEditable){
            btnAddCertificate.setVisibility(View.GONE);
        }
        else{
            btnAddCertificate.setVisibility(View.VISIBLE);
            btnAddCertificate.setOnClickListener(v -> {
                Intent intent = new Intent(StudentDetailActivity.this, AddEditCertificateActivity.class);
                intent.putExtra("studentId", studentId);
                startActivity(intent);
            });
        }

        rvCertificates.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CertificateAdapter(certificateList, isEditable, this);
        rvCertificates.setAdapter(adapter);
        layoutCertImportExport = findViewById(R.id.layoutCertImportExport);
        btnExportCertCsv = findViewById(R.id.btnExportCertCsv);
        btnImportCertCsv = findViewById(R.id.btnImportCertCsv);

        if(!isEditable){
            layoutCertImportExport.setVisibility(View.GONE);
        }
        else{
            layoutCertImportExport.setVisibility(View.VISIBLE);
            btnExportCertCsv.setOnClickListener(v -> {
                if(certificateList.isEmpty()){
                    Toast.makeText(this, "Sinh viên này chưa có chứng chỉ nào để xuất file!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String code = getIntent().getStringExtra("studentCode");
                String name = getIntent().getStringExtra("fullName");
                String safeName = (name != null ? name.replaceAll("[^a-zA-Z0-9]", "_") : "SinhVien");
                String defaultFileName = "Chung_Chi_" + (code != null ? code : "") + "_" + safeName + ".csv";
                exportCertCsvLauncher.launch(defaultFileName);
            });
            btnImportCertCsv.setOnClickListener(v -> importCertCsvLauncher.launch(new String[]{"*/*", "text/csv", "text/comma-separated-values"}));
        }

        loadCertificatesRealtime();
    }

    private void loadCertificatesRealtime(){
        db.collection("students").document(studentId)
                .collection("certificates")
                .addSnapshotListener((value, error) -> {
                    if(error != null){
                        Toast.makeText(this, "Lỗi tải chứng chỉ: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(value != null){
                        certificateList.clear();

                        for(QueryDocumentSnapshot doc : value){
                            Certificate cert = doc.toObject(Certificate.class);
                            cert.setId(doc.getId());
                            certificateList.add(cert);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onEdit(Certificate certificate){
        Intent intent = new Intent(this, AddEditCertificateActivity.class);
        intent.putExtra("studentId", studentId);
        intent.putExtra("certId", certificate.getId());
        intent.putExtra("title", certificate.getTitle());
        intent.putExtra("issuedBy", certificate.getIssuedBy());
        intent.putExtra("issueDate", certificate.getIssueDate());
        intent.putExtra("expiryDate", certificate.getExpiryDate());
        intent.putExtra("description", certificate.getDescription());
        startActivity(intent);
    }

    @Override
    public void onDelete(Certificate certificate){
        new AlertDialog.Builder(this)
                .setTitle("XÁC NHẬN XÓA CHỨNG CHỈ")
                .setMessage("Bạn có chắc chắn muốn xóa chứng chỉ '" + certificate.getTitle() + "' không?")
                .setPositiveButton("XÓA", (dialog, which) -> {
                    db.collection("students").document(studentId)
                            .collection("certificates").document(certificate.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa chứng chỉ!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    private final ActivityResultLauncher<String> exportCertCsvLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"),
            uri -> {
                if(uri != null){
                    try(OutputStream os = getContentResolver().openOutputStream(uri)){
                        CsvUtils.exportCertificatesToCsv(certificateList, os);
                        Toast.makeText(this, "Xuất file CSV chứng chỉ thành công!", Toast.LENGTH_LONG).show();
                    } catch(Exception e){
                        Toast.makeText(this, "Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> importCertCsvLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if(uri != null){
                    try (InputStream is = getContentResolver().openInputStream(uri)){
                        List<Certificate> importedCerts = CsvUtils.importCertificatesFromCsv(is);
                        if(importedCerts.isEmpty()){
                            Toast.makeText(this, "File CSV không chứa dữ liệu chứng chỉ hợp lệ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        saveImportedCertsToFirestore(importedCerts);
                    } catch(Exception e){
                        Toast.makeText(this, "Lỗi nhập file CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void saveImportedCertsToFirestore(List<Certificate> importedCerts) {
        db.collection("students").document(studentId).collection("certificates").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.Set<String> existingTitles = new java.util.HashSet<>();
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        String title = doc.getString("title");
                        if(title != null){
                            existingTitles.add(title.toLowerCase());
                        }
                    }

                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    int addedCount = 0;
                    int skippedCount = 0;
                    java.util.Set<String> csvTitlesInBatch = new java.util.HashSet<>();

                    for(Certificate c : importedCerts){
                        String titleLower = c.getTitle() != null ? c.getTitle().toLowerCase() : "";

                        if(titleLower.isEmpty() || existingTitles.contains(titleLower) || csvTitlesInBatch.contains(titleLower)){
                            skippedCount++;
                            continue;
                        }

                        csvTitlesInBatch.add(titleLower);
                        String newId = db.collection("students").document(studentId).collection("certificates").document().getId();
                        c.setId(newId);
                        c.setStudentId(studentId);
                        batch.set(db.collection("students").document(studentId).collection("certificates").document(newId), c);
                        addedCount++;
                    }

                    if (addedCount == 0) {
                        Toast.makeText(this, "Tất cả chứng chỉ trong file CSV đều đã tồn tại (Đã bỏ qua " + skippedCount + " dòng trùng)!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    int finalAddedCount = addedCount;
                    int finalSkippedCount = skippedCount;

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                String msg = "Đã nhập thành công " + finalAddedCount + " chứng chỉ mới!";
                                if(finalSkippedCount > 0){
                                    msg += " (Đã bỏ qua " + finalSkippedCount + " chứng chỉ trùng)";
                                }
                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kiểm tra dữ liệu cũ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}