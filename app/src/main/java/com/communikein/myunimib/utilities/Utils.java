package com.communikein.myunimib.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import com.communikein.myunimib.User;
import com.google.firebase.crash.FirebaseCrash;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by eliam on 12/2/2017.
 */

public class Utils {
    public static final int OK = 1;

    public static final int HTTP_OK = 200;
    public static final int HTTP_REDIRECT = 302;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final String EXCEPTION_OCCURED = "EXCEPTION_OCCURED";

    public static final SimpleDateFormat sdf = new SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss", Locale.getDefault());
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat(
            "HH:mm", Locale.getDefault());
    public static final DecimalFormat integerFormat = new DecimalFormat("##");
    public static final DecimalFormat markFormat = new DecimalFormat("##.##");

    public static User user;

    public static boolean isInteger(String s) {
        boolean isValidInteger = false;
        try {
            Integer.parseInt(s);

            isValidInteger = true;
        } catch (NumberFormatException ignore) {}

        return isValidInteger;
    }

    public static void hideKeyboard(Context context, boolean hide){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (hide)
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        else
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void saveBugReport(Exception e){
        e.printStackTrace();
        FirebaseCrash.report(e);
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}

