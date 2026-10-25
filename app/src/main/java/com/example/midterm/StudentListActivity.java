package com.example.midterm;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import com.example.midterm.model.Student;
import com.example.midterm.utils.CsvUtils;
import com.example.midterm.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentListActivity extends AppCompatActivity implements StudentAdapter.OnStudentActionListener {
    ImageButton btnBack;
    Button btnAddStudent, btnExportCsv, btnImportCsv;
    TextInputEditText etSearch;
    Spinner spSortCriteria;
    RecyclerView rvStudents;
    FirebaseFirestore db;
    SessionManager sessionManager;
    StudentAdapter adapter;
    private final List<Student> masterStudentList = new ArrayList<>();
    private final List<Student> displayedStudentList = new ArrayList<>();
    LinearLayout layoutImportExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_list);
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

        sessionManager = new SessionManager(this);
        db = FirebaseFirestore.getInstance();
        btnBack = findViewById(R.id.btnBack);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        etSearch = findViewById(R.id.etSearch);
        spSortCriteria = findViewById(R.id.spSortCriteria);
        rvStudents = findViewById(R.id.rvStudents);

        btnBack.setOnClickListener(v -> finish());

        boolean isEditable = sessionManager.isManagerOrAdmin();

        if(!isEditable){
            btnAddStudent.setVisibility(View.GONE);
        }
        else{
            btnAddStudent.setVisibility(View.VISIBLE);
            btnAddStudent.setOnClickListener(v -> startActivity(new Intent(StudentListActivity.this, AddEditStudentActivity.class)));
        }

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(displayedStudentList, isEditable, this);
        rvStudents.setAdapter(adapter);
        String[] sortOptions = {
                "Tên (A -> Z)",
                "Tên (Z -> A)",
                "GPA (Cao -> Thấp)",
                "GPA (Thấp -> Cao)",
                "Mã SV (A -> Z)",
                "Mã SV (Z -> A)",
                "Tuổi (Tăng dần)",
                "Tuổi (Giảm dần)"
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spSortCriteria.setAdapter(sortAdapter);

        spSortCriteria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortStudents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSortStudents();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        layoutImportExport = findViewById(R.id.layoutImportExport);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnImportCsv = findViewById(R.id.btnImportCsv);

        if(!isEditable){
            layoutImportExport.setVisibility(View.GONE);
        }
        else{
            layoutImportExport.setVisibility(View.VISIBLE);
            btnExportCsv.setOnClickListener(v -> {
                if(displayedStudentList.isEmpty()){
                    Toast.makeText(this, "Danh sách sinh viên đang rỗng, không có dữ liệu để xuất file!", Toast.LENGTH_SHORT).show();
                    return;
                }
                exportCsvLauncher.launch("Danh_Sach_Sinh_Vien.csv");
            });
            btnImportCsv.setOnClickListener(v -> importCsvLauncher.launch(new String[]{"*/*", "text/csv", "text/comma-separated-values"}));
        }

        loadStudentsRealtime();
    }

    private void loadStudentsRealtime(){
        db.collection("students").addSnapshotListener((value, error) -> {
            if(error != null){
                Toast.makeText(this, "Lỗi tải sinh viên: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if(value != null){
                masterStudentList.clear();

                for(QueryDocumentSnapshot doc : value){
                    Student student = doc.toObject(Student.class);
                    student.setId(doc.getId());
                    masterStudentList.add(student);
                }

                filterAndSortStudents();
            }
        });
    }

    private void filterAndSortStudents(){
        String rawQuery = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        String normalizedQuery = removeAccents(rawQuery);
        displayedStudentList.clear();

        for (Student s : masterStudentList) {
            String normName = removeAccents(s.getFullName());
            String normCode = removeAccents(s.getStudentCode());
            String normClass = removeAccents(s.getClassId());
            boolean matchName = normName.contains(normalizedQuery);
            boolean matchCode = normCode.contains(normalizedQuery);
            boolean matchClass = normClass.contains(normalizedQuery);

            if (matchName || matchCode || matchClass) {
                displayedStudentList.add(s);
            }
        }

        int sortPosition = spSortCriteria.getSelectedItemPosition();
        switch (sortPosition) {
            case 0:
                Collections.sort(displayedStudentList, (a, b) -> compareVietnameseNames(a.getFullName(), b.getFullName()));                break;
            case 1:
                Collections.sort(displayedStudentList, (a, b) -> compareVietnameseNames(b.getFullName(), a.getFullName()));                break;
            case 2:
                Collections.sort(displayedStudentList, (a, b) -> Double.compare(b.getGpa(), a.getGpa()));
                break;
            case 3:
                Collections.sort(displayedStudentList, (a, b) -> Double.compare(a.getGpa(), b.getGpa()));
                break;
            case 4:
                Collections.sort(displayedStudentList, (a, b) -> a.getStudentCode().compareToIgnoreCase(b.getStudentCode()));
                break;
            case 5:
                Collections.sort(displayedStudentList, (a, b) -> b.getStudentCode().compareToIgnoreCase(a.getStudentCode()));
                break;
            case 6:
                Collections.sort(displayedStudentList, (a, b) -> Integer.compare(a.getAge(), b.getAge()));
                break;
            case 7:
                Collections.sort(displayedStudentList, (a, b) -> Integer.compare(b.getAge(), a.getAge()));
                break;
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewDetails(Student student) {
        Intent intent = new Intent(this, StudentDetailActivity.class);
        intent.putExtra("studentId", student.getId());
        intent.putExtra("studentCode", student.getStudentCode());
        intent.putExtra("fullName", student.getFullName());
        intent.putExtra("age", student.getAge());
        intent.putExtra("gender", student.getGender());
        intent.putExtra("classId", student.getClassId());
        intent.putExtra("gpa", student.getGpa());
        intent.putExtra("email", student.getEmail());
        intent.putExtra("phone", student.getPhone());
        startActivity(intent);
    }

    @Override
    public void onEdit(Student student){
        Intent intent = new Intent(this, AddEditStudentActivity.class);
        intent.putExtra("studentId", student.getId());
        intent.putExtra("studentCode", student.getStudentCode());
        intent.putExtra("fullName", student.getFullName());
        intent.putExtra("age", student.getAge());
        intent.putExtra("gender", student.getGender());
        intent.putExtra("classId", student.getClassId());
        intent.putExtra("gpa", student.getGpa());
        intent.putExtra("email", student.getEmail());
        intent.putExtra("phone", student.getPhone());
        startActivity(intent);
    }

    @Override
    public void onDelete(Student student){
        new AlertDialog.Builder(this)
                .setTitle("XÁC NHẬN XÓA SINH VIÊN")
                .setMessage("Bạn có chắc chắn muốn xóa sinh viên " + student.getFullName() + " (" + student.getStudentCode() + ") không?")
                .setPositiveButton("XÓA", (dialog, which) -> {
                    db.collection("students").document(student.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa sinh viên!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    private final ActivityResultLauncher<String> exportCsvLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"),
            uri -> {
                if(uri != null){
                    try(OutputStream os = getContentResolver().openOutputStream(uri)){
                        CsvUtils.exportStudentsToCsv(displayedStudentList, os);
                        Toast.makeText(this, "Xuất file CSV Sinh viên thành công!", Toast.LENGTH_LONG).show();
                    } catch(Exception e){
                        Toast.makeText(this, "Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> importCsvLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if(uri != null){
                    try(InputStream is = getContentResolver().openInputStream(uri)){
                        List<Student> importedStudents = CsvUtils.importStudentsFromCsv(is);
                        if(importedStudents.isEmpty()){
                            Toast.makeText(this, "File CSV không chứa dữ liệu hợp lệ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        saveImportedStudentsToFirestore(importedStudents);
                    } catch(Exception e){
                        Toast.makeText(this, "Lỗi nhập file CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void saveImportedStudentsToFirestore(List<Student> importedStudents) {
        db.collection("students").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.Set<String> existingCodes = new java.util.HashSet<>();

                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        String code = doc.getString("studentCode");

                        if(code != null){
                            existingCodes.add(code.toLowerCase());
                        }
                    }

                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    int addedCount = 0;
                    int skippedCount = 0;
                    java.util.Set<String> csvCodesInBatch = new java.util.HashSet<>();

                    for(Student s : importedStudents){
                        String codeLower = s.getStudentCode() != null ? s.getStudentCode().toLowerCase() : "";

                        if(codeLower.isEmpty() || existingCodes.contains(codeLower) || csvCodesInBatch.contains(codeLower)){
                            skippedCount++;
                            continue;
                        }

                        csvCodesInBatch.add(codeLower);
                        String newId = db.collection("students").document().getId();
                        s.setId(newId);
                        batch.set(db.collection("students").document(newId), s);
                        addedCount++;
                    }

                    if(addedCount == 0){
                        Toast.makeText(this, "Tất cả mã sinh viên trong file CSV đều đã tồn tại (Đã bỏ qua " + skippedCount + " dòng trùng)!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    int finalAddedCount = addedCount;
                    int finalSkippedCount = skippedCount;

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                String msg = "Đã nhập thành công " + finalAddedCount + " sinh viên mới!";
                                if(finalSkippedCount > 0){
                                    msg += " (Đã bỏ qua " + finalSkippedCount + " mã SV trùng)";
                                }
                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kiểm tra dữ liệu cũ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String removeAccents(String str) {
        if (str == null) return "";
        String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        return temp.replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }

    private int compareVietnameseNames(String fullName1, String fullName2) {
        if(fullName1 == null || fullName1.trim().isEmpty()){
            return -1;
        }

        if(fullName2 == null || fullName2.trim().isEmpty()){
            return 1;
        }

        String name1 = removeAccents(fullName1.trim());
        String name2 = removeAccents(fullName2.trim());
        String[] parts1 = name1.split("\\s+");
        String[] parts2 = name2.split("\\s+");
        String firstName1 = parts1[parts1.length - 1];
        String firstName2 = parts2[parts2.length - 1];
        int result = firstName1.compareToIgnoreCase(firstName2);

        if(result != 0){
            return result;
        }

        return name1.compareToIgnoreCase(name2);
    }
}