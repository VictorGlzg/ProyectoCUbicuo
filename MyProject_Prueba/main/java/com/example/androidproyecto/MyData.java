package com.example.androidproyecto;

import java.util.ArrayList;

public class MyData {
    ArrayList<Integer> update_id;
    ArrayList<String> msg;

    MyData(){
        initialize();
    }

    public void initialize(){
        update_id = new ArrayList<Integer>();
        msg = new ArrayList<String>();
    }
}
