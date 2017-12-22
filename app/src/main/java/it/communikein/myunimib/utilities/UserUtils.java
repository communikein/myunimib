package it.communikein.myunimib.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import it.communikein.myunimib.data.User;


public class UserUtils {

    public static User updateSessionId(User user, String cookie, Context context) {
        if (cookie != null && cookie.contains("JSESSIONID")) {
            // Save it
            cookie = cookie.substring(cookie.indexOf("JSESSIONID=") + 11);
            cookie = cookie.substring(0, cookie.indexOf(";"));
            user.setSessionID(cookie);
            UserUtils.saveUser(user, context);
        }

        return user;
    }


    public static void saveUser(User user, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(User.PREFERENCES_USER,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(User.PREF_USERNAME, user.getUsername());
        editor.putString(User.PREF_PASSWORD, user.getPassword());
        editor.putString(User.PREF_SESSION_ID, user.getSessionID());

        editor.putString(User.PREF_MATRICOLA, user.getMatricola());
        editor.putString(User.PREF_NAME, user.getName());

        editor.putInt(User.PREF_TOTAL_CFU, user.getTotalCFU());
        editor.putFloat(User.PREF_AVERAGE_MARK, user.getAverageMark());

        if (user.hasFacultiesList()) {
            editor.putString(User.PREF_FACULTIES, user.getFacultiesJSON().toString());
            editor.putInt(User.PREF_SELECTED_FACULTY, user.getSelectedFaculty());
        } else {
            editor.remove(User.PREF_FACULTIES);
            editor.remove(User.PREF_FACULTIES);
        }

        editor.putBoolean(User.PREF_FAKE, user.isFake());

        editor.apply();
    }

    public static void removeUser(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(User.PREFERENCES_USER,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.remove(User.PREF_USERNAME);
        editor.remove(User.PREF_PASSWORD);
        editor.remove(User.PREF_SESSION_ID);

        editor.remove(User.PREF_NAME);
        editor.remove(User.PREF_MATRICOLA);
        editor.remove(User.PREF_AVERAGE_MARK);
        editor.remove(User.PREF_TOTAL_CFU);

        editor.remove(User.PREF_FACULTIES_KEYS);
        editor.remove(User.PREF_FACULTIES_VALUES);
        editor.remove(User.PREF_SELECTED_FACULTY);
        editor.remove(User.PREF_FAKE);
        editor.apply();

        Utils.user = null;
    }

    public static User getUser(Context context){
        SharedPreferences pref = context.getSharedPreferences(User.PREFERENCES_USER,
                Context.MODE_PRIVATE);

        if (Utils.user == null && pref.contains(User.PREF_USERNAME)){
            String username, password, name, matricola, session_id;
            boolean isFake;
            int total_cfu;
            float average_mark;

            username = pref.getString(User.PREF_USERNAME, null);
            password = pref.getString(User.PREF_PASSWORD, null);
            session_id = pref.getString(User.PREF_SESSION_ID, null);

            name = pref.getString(User.PREF_NAME, null);
            matricola = pref.getString(User.PREF_MATRICOLA, null);

            total_cfu = pref.getInt(User.PREF_TOTAL_CFU, User.ERROR_TOTAL_CFU);
            average_mark = pref.getFloat(User.PREF_AVERAGE_MARK, User.ERROR_AVERAGE_MARK);

            isFake = pref.getBoolean(User.PREF_FAKE, false);

            Utils.user = new User(username, password, name, average_mark, total_cfu, matricola);
            Utils.user.setSessionID(session_id);
            Utils.user.setFake(isFake);

            String faculties_tmp = pref.getString(User.PREF_FACULTIES, null);
            if (faculties_tmp != null) {
                try {
                    JSONObject faculties_json = new JSONObject(faculties_tmp);
                    Utils.user.setFaculties(faculties_json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (pref.contains(User.PREF_SELECTED_FACULTY)) {
                int selectedFaculty = pref.getInt(
                        User.PREF_SELECTED_FACULTY,
                        -1);
                Utils.user.setSelectedFaculty(selectedFaculty);
            }

            return Utils.user;
        }

        return Utils.user;
    }

}
