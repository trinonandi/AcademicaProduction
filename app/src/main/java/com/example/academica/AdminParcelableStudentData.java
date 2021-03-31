package com.example.academica;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class AdminParcelableStudentData implements Parcelable {
    private HashMap<String,String> studentDataMap;
    private String dept, sem;

    public AdminParcelableStudentData(HashMap<String, String> studentDataMap, String dept, String sem){
        this.studentDataMap = studentDataMap;
        this.dept = dept;
        this.sem = sem;
    }

    protected AdminParcelableStudentData(Parcel in) {
        this.studentDataMap = in.readHashMap(null);
        this.dept = in.readString();
        this.sem = in.readString();
    }

    public static final Creator<AdminParcelableStudentData> CREATOR = new Creator<AdminParcelableStudentData>() {
        @Override
        public AdminParcelableStudentData createFromParcel(Parcel in) {
            return new AdminParcelableStudentData(in);
        }

        @Override
        public AdminParcelableStudentData[] newArray(int size) {
            return new AdminParcelableStudentData[size];
        }
    };

    // getters
    public HashMap<String, String> getStudentDataMap() {
        return studentDataMap;
    }

    public String getDept() {
        return dept;
    }

    public String getSem() {
        return sem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(studentDataMap);
        dest.writeString(dept);
        dest.writeString(sem);
    }
}