package com.example.midterm;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.midterm.model.Student;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AddEditStudentActivity extends AppCompatActivity {
    ImageButton btnBack;
    TextView tvHeaderTitle;
    TextInputEditText etStudentCode, etFullName, etAge, etClassId, etGpa, etEmail, etPhone;
    Spinner spGender;
    Button btnSaveStudent;
    FirebaseFirestore db;
    String studentId = null;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_student);
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
        btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        etStudentCode = findViewById(R.id.etStudentCode);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        spGender = findViewById(R.id.spGender);
        etClassId = findViewById(R.id.etClassId);
        etGpa = findViewById(R.id.etGpa);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSaveStudent = findViewById(R.id.btnSaveStudent);

        btnBack.setOnClickListener(v -> finish());

        String[] genders = {"Nam", "Nữ"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, genders);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        if(getIntent().hasExtra("studentId")){
            isEditMode = true;
            studentId = getIntent().getStringExtra("studentId");
            tvHeaderTitle.setText("Chỉnh sửa Sinh viên");
            btnSaveStudent.setText("CẬP NHẬT THÔNG TIN");
            etStudentCode.setText(getIntent().getStringExtra("studentCode"));
            etFullName.setText(getIntent().getStringExtra("fullName"));
            etAge.setText(String.valueOf(getIntent().getIntExtra("age", 20)));
            etClassId.setText(getIntent().getStringExtra("classId"));
            etGpa.setText(String.valueOf(getIntent().getDoubleExtra("gpa", 0.0)));
            etEmail.setText(getIntent().getStringExtra("email"));
            etPhone.setText(getIntent().getStringExtra("phone"));
            String gender = getIntent().getStringExtra("gender");

            if("Nữ".equalsIgnoreCase(gender)){
                spGender.setSelection(1);
            }
            else{
                spGender.setSelection(0);
            }
        }
        else{
            tvHeaderTitle.setText("Thêm Sinh viên Mới");
            btnSaveStudent.setText("LƯU SINH VIÊN");
        }

        btnSaveStudent.setOnClickListener(v -> saveStudent());
    }

    private void saveStudent(){
        String code = etStudentCode.getText() != null ? etStudentCode.getText().toString().trim() : "";
        String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String gender = spGender.getSelectedItem().toString();
        String classId = etClassId.getText() != null ? etClassId.getText().toString().trim() : "";
        String gpaStr = etGpa.getText() != null ? etGpa.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if(TextUtils.isEmpty(code)){
            etStudentCode.setError("Mã sinh viên không được để trống!");
            etStudentCode.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(name)){
            etFullName.setError("Họ và tên không được để trống!");
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

            if(age < 17 || age > 100){
                etAge.setError("Tuổi sinh viên phải từ 17 đến 100!");
                etAge.requestFocus();
                return;
            }
        } catch(Exception e){
            etAge.setError("Tuổi không hợp lệ!");
            etAge.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(classId)){
            etClassId.setError("Lớp học không được để trống!");
            etClassId.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(gpaStr)){
            etGpa.setError("Vui lòng nhập điểm GPA!");
            etGpa.requestFocus();
            return;
        }

        double gpa;

        try{
            gpa = Double.parseDouble(gpaStr);
            if (gpa < 0.0 || gpa > 4.0) {
                etGpa.setError("Điểm GPA phải nằm trong khoảng từ 0.0 đến 4.0!");
                etGpa.requestFocus();
                return;
            }
        } catch(Exception e){
            etGpa.setError("Điểm GPA không hợp lệ!");
            etGpa.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(email)){
            etEmail.setError("Email không được để trống!");
            etEmail.requestFocus();
            return;
        }

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Địa chỉ Email không đúng định dạng!");
            etEmail.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(phone)){
            etPhone.setError("Số điện thoại không được để trống!");
            etPhone.requestFocus();
            return;
        }

        if(!phone.matches("^0[0-9]{9}$")){
            etPhone.setError("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0!");
            etPhone.requestFocus();
            return;
        }

        btnSaveStudent.setEnabled(false);
        db.collection("students")
                .whereEqualTo("studentCode", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        if(!isEditMode || !doc.getId().equals(studentId)){
                            isDuplicate = true;
                            break;
                        }
                    }

                    if(isDuplicate){
                        btnSaveStudent.setEnabled(true);
                        etStudentCode.setError("Mã sinh viên '" + code + "' đã tồn tại ở một sinh viên khác!");
                        etStudentCode.requestFocus();
                        return;
                    }

                    if(isEditMode && studentId != null){
                        db.collection("students").document(studentId)
                                .update(
                                        "studentCode", code,
                                        "fullName", name,
                                        "age", age,
                                        "gender", gender,
                                        "classId", classId,
                                        "gpa", gpa,
                                        "email", email,
                                        "phone", phone
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Cập nhật sinh viên thành công!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnSaveStudent.setEnabled(true);
                                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                    else{
                        String newId = db.collection("students").document().getId();
                        Student newStudent = new Student(newId, code, name, age, gender, email, phone, classId, gpa, System.currentTimeMillis());
                        db.collection("students").document(newId).set(newStudent)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Thêm sinh viên thành công!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnSaveStudent.setEnabled(true);
                                    Toast.makeText(this, "Lỗi thêm sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    btnSaveStudent.setEnabled(true);
                    Toast.makeText(this, "Lỗi kiểm tra trùng mã: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}