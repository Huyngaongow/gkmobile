package com.example.midterm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.model.Student;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentActionListener{
        void onViewDetails(Student student);
        void onEdit(Student student);
        void onDelete(Student student);
    }

    private final List<Student> studentList;
    private final boolean isEditable;
    private final OnStudentActionListener listener;

    public StudentAdapter(List<Student> studentList, boolean isEditable, OnStudentActionListener listener){
        this.studentList = studentList;
        this.isEditable = isEditable;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvStudentCode.setText(student.getStudentCode());
        holder.tvFullName.setText(student.getFullName());
        holder.tvClassAndAge.setText("Lớp: " + student.getClassId() + " • " + student.getAge() + " tuổi (" + student.getGender() + ")");
        holder.tvGpa.setText(String.format("%.1f", student.getGpa()));

        if(!isEditable){
            holder.btnEditStudent.setVisibility(View.GONE);
            holder.btnDeleteStudent.setVisibility(View.GONE);
        }
        else{
            holder.btnEditStudent.setVisibility(View.VISIBLE);
            holder.btnDeleteStudent.setVisibility(View.VISIBLE);
        }

        holder.btnViewDetails.setOnClickListener(v -> listener.onViewDetails(student));
        holder.btnEditStudent.setOnClickListener(v -> listener.onEdit(student));
        holder.btnDeleteStudent.setOnClickListener(v -> listener.onDelete(student));
    }

    @Override
    public int getItemCount(){
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder{
        TextView tvStudentCode, tvFullName, tvClassAndAge, tvGpa;
        Button btnViewDetails, btnEditStudent, btnDeleteStudent;

        public StudentViewHolder(@NonNull View itemView){
            super(itemView);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvClassAndAge = itemView.findViewById(R.id.tvClassAndAge);
            tvGpa = itemView.findViewById(R.id.tvGpa);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnEditStudent = itemView.findViewById(R.id.btnEditStudent);
            btnDeleteStudent = itemView.findViewById(R.id.btnDeleteStudent);
        }
    }
}