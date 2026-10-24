package com.example.midterm.model;

public class Certificate {
    private String id;
    private String studentId;
    private String title;
    private String issuedBy;
    private String issueDate;
    private String expiryDate;
    private String description;

    public Certificate(){
    }

    public Certificate(String id, String studentId, String title, String issuedBy, String issueDate, String expiryDate, String description){
        this.id = id;
        this.studentId = studentId;
        this.title = title;
        this.issuedBy = issuedBy;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.description = description;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getStudentId(){
        return studentId;
    }
    public void setStudentId(String studentId){
        this.studentId = studentId;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getIssuedBy(){
        return issuedBy;
    }
    public void setIssuedBy(String issuedBy){
        this.issuedBy = issuedBy;
    }

    public String getIssueDate(){
        return issueDate;
    }
    public void setIssueDate(String issueDate){
        this.issueDate = issueDate;
    }

    public String getExpiryDate(){
        return expiryDate;
    }
    public void setExpiryDate(String expiryDate){
        this.expiryDate = expiryDate;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
}