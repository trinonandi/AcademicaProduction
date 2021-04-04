package com.example.academica.Student;

import java.io.Serializable;

public class StudentRegDataHelper implements Serializable {
    // a helper class to fetch student user data from firebase database
    private String fullName, email, classRoll, univRoll, sem, dept,type;

    public StudentRegDataHelper(){
        // this is mandatory for firebase passing
    }

    public StudentRegDataHelper(String fullName, String email, String classRoll, String univRoll, String sem, String dept){
        this.fullName = fullName;
        this.email = email;
        this.classRoll = classRoll;
        this.univRoll = univRoll;
        this.sem = sem;
        this.dept = dept;
        this.type="STUDENT";
    }

    public static String generateKeyFromEmail(String email){
        String[] nameFormEmail = email.split("@"); // extracts the name from the email id
        String key = nameFormEmail[0].replaceAll("\\.",","); // replaces invalid chars
        key = key.replaceAll("\\$",",");    // replaces invalid chars
        key = key.replaceAll("#",",");  // replaces invalid chars

        return key;
    }

    public String getClassRoll() {
        return classRoll;
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

    public String getSem() {
        return sem;
    }

    public String getUnivRoll() {
        return univRoll;
    }

    public String getType(){return type ;}

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setClassRoll(String classRoll) {
        this.classRoll = classRoll;
    }

    public void setUnivRoll(String univRoll) {
        this.univRoll = univRoll;
    }

    public void setSem(String sem) {
        this.sem = sem;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setType(String type){this.type = type;}
}
