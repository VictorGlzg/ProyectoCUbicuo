package com.example.myproject;

import android.widget.EditText;
import android.widget.TextView;

public class SharedData {
    private static String result = "";
    private static TextView tvResponse;
    private static EditText etMessage;

    public static String getResult() {
        return result;
    }

    public static void setResult(String newResult) {
        result = newResult;
    }

}


