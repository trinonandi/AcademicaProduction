package com.example.academica;

public class RecyclerItem {
    private String key;
    private String name;

    public RecyclerItem(String key, String name){
        this.name = name;
        this.key = key;
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
