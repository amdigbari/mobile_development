package com.example.hw2;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class Utils {
    public static void closeKeyboard(View view, Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && view.getWindowToken() != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
