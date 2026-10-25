package com.example.midterm.utils;

import com.example.midterm.model.Certificate;
import com.example.midterm.model.Student;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvUtils {
    private static String removeAccents(String str) {
        if(str == null){
            return "";
        }
        String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        return temp.replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }
    public static void exportStudentsToCsv(List<Student> studentList, OutputStream outputStream) throws Exception{
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        writer.write('\uFEFF');
        writer.println("Mã SV,Họ và Tên,Tuổi,Giới tính,Lớp,GPA,Email,Số điện thoại");

        for(Student s : studentList){
            writer.println(String.format(Locale.US, "\"%s\",\"%s\",%d,\"%s\",\"%s\",%.1f,\"%s\",\"%s\"",
                    cleanCsvValue(s.getStudentCode()),
                    cleanCsvValue(s.getFullName()),
                    s.getAge(),
                    cleanCsvValue(s.getGender()),
                    cleanCsvValue(s.getClassId()),
                    s.getGpa(),
                    cleanCsvValue(s.getEmail()),
                    cleanCsvValue(s.getPhone())
            ));
        }

        writer.flush();
        writer.close();
    }

    public static List<Student> importStudentsFromCsv(InputStream inputStream) throws Exception{
        List<Student> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        boolean isHeader = true;

        while((line = reader.readLine()) != null){
            if(line.startsWith("\uFEFF")){
                line = line.substring(1);
            }

            if(line.trim().isEmpty()){
                continue;
            }

            if (isHeader) {
                isHeader = false;
                String headerNorm = removeAccents(line);

                if(headerNorm.contains("ten chung chi") || headerNorm.contains("noi cap") || (!headerNorm.contains("ma sv") && !headerNorm.contains("gpa"))){
                    reader.close();
                    throw new IllegalArgumentException("File CSV này không chứa nội dung danh sách sinh viên!");
                }

                continue;
            }

            String[] tokens = parseCsvLine(line);

            if(tokens.length >= 8){
                String code = tokens[0].trim();
                String name = tokens[1].trim();
                int age = parseSafeInt(tokens[2].trim(), 20);
                String gender = tokens[3].trim();
                String classId = tokens[4].trim();
                double gpa = parseSafeDouble(tokens[5].trim(), 0.0);
                String email = tokens[6].trim();
                String phone = tokens[7].trim();

                if(!code.isEmpty() && !name.isEmpty()){
                    Student student = new Student("", code, name, age, gender, email, phone, classId, gpa, System.currentTimeMillis());
                    list.add(student);
                }
            }
        }

        reader.close();
        return list;
    }

    public static void exportCertificatesToCsv(List<Certificate> certList, OutputStream outputStream) throws Exception{
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        writer.write('\uFEFF');
        writer.println("Tên chứng chỉ,Nơi cấp,Ngày cấp,Ngày hết hạn,Ghi chú");

        for(Certificate c : certList){
            writer.println(String.format(Locale.US, "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    cleanCsvValue(c.getTitle()),
                    cleanCsvValue(c.getIssuedBy()),
                    cleanCsvValue(c.getIssueDate()),
                    cleanCsvValue(c.getExpiryDate()),
                    cleanCsvValue(c.getDescription())
            ));
        }

        writer.flush();
        writer.close();
    }

    public static List<Certificate> importCertificatesFromCsv(InputStream inputStream) throws Exception{
        List<Certificate> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        boolean isHeader = true;

        while((line = reader.readLine()) != null){
            if(line.startsWith("\uFEFF")){
                line = line.substring(1);
            }

            if(line.trim().isEmpty()){
                continue;
            }

            if (isHeader) {
                isHeader = false;
                String headerNorm = removeAccents(line);

                if(headerNorm.contains("ma sv") || headerNorm.contains("gpa") || (!headerNorm.contains("ten chung chi") && !headerNorm.contains("noi cap"))){
                    reader.close();
                    throw new IllegalArgumentException("File CSV này không chứa nội dung chứng chỉ!");
                }

                continue;
            }

            String[] tokens = parseCsvLine(line);
            if(tokens.length >= 3){
                String title = tokens[0].trim();
                String issuedBy = tokens[1].trim();
                String issueDate = tokens[2].trim();
                String expiryDate = tokens.length > 3 ? tokens[3].trim() : "";
                String description = tokens.length > 4 ? tokens[4].trim() : "";

                if(!title.isEmpty() && !issuedBy.isEmpty()){
                    Certificate cert = new Certificate("", "", title, issuedBy, issueDate, expiryDate, description);
                    list.add(cert);
                }
            }
        }

        reader.close();
        return list;
    }

    private static String cleanCsvValue(String value){
        if (value == null){
            return "";
        }
        return value.replace("\"", "\"\"");
    }

    private static String[] parseCsvLine(String line){
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < line.length(); i++){
            char c = line.charAt(i);

            if(c == '"'){
                inQuotes = !inQuotes;
            }
            else if(c == ',' && !inQuotes){
                result.add(sb.toString().replace("\"", ""));
                sb.setLength(0);
            }
            else{
                sb.append(c);
            }
        }

        result.add(sb.toString().replace("\"", ""));
        return result.toArray(new String[0]);
    }

    private static int parseSafeInt(String val, int def) {
        try{
            return Integer.parseInt(val);
        } catch(Exception e){
            return def;
        }
    }

    private static double parseSafeDouble(String val, double def) {
        try{
            return Double.parseDouble(val);
        } catch(Exception e){
            return def;
        }
    }
}