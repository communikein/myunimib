package it.communikein.myunimib.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import it.communikein.myunimib.User;
import com.google.firebase.crash.FirebaseCrash;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class Utils {

    public static final SimpleDateFormat sdf = new SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss", Locale.getDefault());
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat(
            "HH:mm", Locale.getDefault());
    public static final DecimalFormat integerFormat = new DecimalFormat("##");
    public static final DecimalFormat markFormat = new DecimalFormat("##.##");

    public static User user;

    public static void hideKeyboard(Context context){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (imm != null)
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void saveBugReport(Exception e, String TAG){
        e.printStackTrace();
        FirebaseCrash.logcat(Log.ERROR, TAG, e.getMessage());
        FirebaseCrash.report(e);
    }
}

