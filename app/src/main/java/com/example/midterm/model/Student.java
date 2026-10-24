package com.example.midterm.model;

public class Student {
    private String id;
    private String studentCode;
    private String fullName;
    private int age;
    private String gender;
    private String email;
    private String phone;
    private String classId;
    private double gpa;
    private long createdAt;

    public Student(){
    }

    public Student(String id, String studentCode, String fullName, int age, String gender, String email, String phone, String classId, double gpa, long createdAt){
        this.id = id;
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.classId = classId;
        this.gpa = gpa;
        this.createdAt = createdAt;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getStudentCode(){
        return studentCode;
    }
    public void setStudentCode(String studentCode){
        this.studentCode = studentCode;
    }

    public String getFullName(){
        return fullName;
    }
    public void setFullName(String fullName){
        this.fullName = fullName;
    }

    public int getAge(){
        return age;
    }
    public void setAge(int age){
        this.age = age;
    }

    public String getGender(){
        return gender;
    }
    public void setGender(String gender){
        this.gender = gender;
    }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getClassId(){
        return classId;
    }
    public void setClassId(String classId){
        this.classId = classId;
    }

    public double getGpa(){
        return gpa;
    }
    public void setGpa(double gpa){
        this.gpa = gpa;
    }

    public long getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(long createdAt){
        this.createdAt = createdAt;
    }
}