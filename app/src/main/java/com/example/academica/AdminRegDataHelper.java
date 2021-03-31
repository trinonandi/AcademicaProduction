package com.example.academica;

import java.io.Serializable;

public class AdminRegDataHelper implements Serializable {
    // a helper class to fetch student user data from firebase database
    String fullName, email, type;

    public AdminRegDataHelper(){
        // this is mandatory for firebase passing
    }

    public AdminRegDataHelper(String fullName, String email){
        this.fullName = fullName;
        this.email = email;


        this.type="ADMIN";
    }

    public static String generateKeyFromEmail(String email){
        String[] nameFormEmail = email.split("@"); // extracts the name from the email id
        String key = nameFormEmail[0].replaceAll("\\.",","); // replaces invalid chars
        key = key.replaceAll("\\$",",");    // replaces invalid chars
        key = key.replaceAll("#",",");  // replaces invalid chars

        return key;
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




    public void setType(String type){this.type = type;}
}
