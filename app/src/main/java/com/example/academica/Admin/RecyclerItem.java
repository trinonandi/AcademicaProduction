package com.example.academica.Admin;

public class RecyclerItem {
    private String key;
    private String name;

    public RecyclerItem(){}

    public RecyclerItem(String key, String name){
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setKey(String key){
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }
}
