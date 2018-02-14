package it.communikein.myunimib.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.crash.FirebaseCrash;

import java.text.DecimalFormat;


public class Utils {

    public static final String UNIMIB_WEBSITE = "https://s3w.si.unimib.it/esse3/";
    private static final String PLAY_STORE_URL =
            "https://play.google.com/store/apps/details?id=it.communikein.myunimib";
    private static final String PLAY_STORE_URI = "market://details?id=it.communikein.myunimib";
    private static final String ENTER_BETA_URL =
            "https://play.google.com/apps/testing/it.communikein.myunimib";
    public static final String TERMS_CONDITIONS_URL = "http://txt.do/23mn";

    public static final DecimalFormat integerFormat = new DecimalFormat("##");
    public static final DecimalFormat markFormat = new DecimalFormat("###.##");


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

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);

            return true;
        } catch (NumberFormatException ignore) {
            return false;
        }
    }

    public static void showAppInStore(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(PLAY_STORE_URI)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(PLAY_STORE_URL)));
        }
    }

    public static void showBetaProgram(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(Utils.ENTER_BETA_URL)));
    }
}

