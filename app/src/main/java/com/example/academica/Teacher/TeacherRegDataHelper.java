package com.example.academica.Teacher;

import java.io.Serializable;

public class TeacherRegDataHelper implements Serializable {
    // a helper class to fetch student user data from firebase database
    String fullName, email,  dept,type;

    public TeacherRegDataHelper(){
        // this is mandatory for firebase passing
    }

    public TeacherRegDataHelper(String fullName, String email,  String dept){
        this.fullName = fullName;
        this.email = email;

        this.dept = dept;
        this.type="TEACHER";
    }

    public static String generateKeyFromEmail(String email){
        String[] nameFormEmail = email.split("@"); // extracts the name from the email id
        String key = nameFormEmail[0].replaceAll("\\.",","); // replaces invalid chars
        key = key.replaceAll("\\$",",");    // replaces invalid chars
        key = key.replaceAll("#",",");  // replaces invalid chars

        return key;
    }



    public String getDept() {
        return dept;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getType(){return type ;}



    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public void setDept(String dept) {
        this.dept = dept;
    }
    public void setType(String type){this.type = type;}
}
