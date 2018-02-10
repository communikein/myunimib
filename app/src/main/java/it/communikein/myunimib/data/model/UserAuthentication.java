package it.communikein.myunimib.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.communikein.myunimib.data.network.loaders.S3Helper;

public class UserAuthentication {

    public static final String PREF_USERNAME = "user_username";
    public static final String PREF_PASSWORD = "user_password";
    public static final String PREF_SESSION_ID = "user_session_id";
    public static final String PREF_FACULTIES = "user_faculties";
    public static final String PREF_FACULTIES_KEYS = "user_faculties_keys";
    public static final String PREF_FACULTIES_VALUES = "user_faculties_values";
    public static final String PREF_SELECTED_FACULTY = "selected_faculty";
    public static final String PREF_FAKE = "user_fake";


    private static final int FACULTY_NOT_CHOSEN = -1;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "username")
    private String mUsername;
    @Ignore
    private String mPassword;
    @Ignore
    private String mAuthToken;
    @ColumnInfo(name = "sessionID")
    private String mSessionID;
    @ColumnInfo(name = "faculties")
    private SparseArray<String> mFaculties;
    @ColumnInfo(name = "selectedFaculty")
    private int mSelectedFaculty;
    @ColumnInfo(name = "fake")
    private boolean mFake;

    public UserAuthentication(String username, String sessionID,
                              SparseArray<String> faculties, int selectedFaculty,
                              boolean fake) {
        setUsername(username);
        setPassword("");
        setAuthToken(username, "");
        setSessionID(sessionID);
        setFaculties(faculties);
        setSelectedFaculty(selectedFaculty);
        setFake(fake);
    }

    @Ignore
    public UserAuthentication(String username, String password, String sessionID,
                              SparseArray<String> faculties, int selectedFaculty,
                              boolean isFake) {
        setUsername(username);
        setPassword(password);
        setAuthToken(username, password);
        setSessionID(sessionID);
        setFaculties(faculties);
        setSelectedFaculty(selectedFaculty);
        setFake(isFake);
    }



    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        if (username != null) this.mUsername = username;
        else this.mUsername = "";
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        if (password != null) {
            this.mPassword = password;
            setAuthToken(getUsername(), password);
        }
        else this.mPassword = "";
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public void setAuthToken(String authToken) {
        this.mAuthToken = authToken;
    }

    public boolean isAuthTokenSet() {
        return !TextUtils.isEmpty(getAuthToken());
    }

    @Ignore
    private void setAuthToken(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) this.mAuthToken = null;

        String authString = username + ":" + password;
        byte[] authEncBytes = Base64.encode(authString.getBytes(), Base64.NO_WRAP);
        this.mAuthToken = new String(authEncBytes);
    }

    public String getSessionID() {
        return mSessionID;
    }

    public void setSessionID(String sessionID) {
        if (sessionID != null) this.mSessionID = sessionID;
        else this.mSessionID = "";
    }

    public SparseArray<String> getFaculties() {
        return mFaculties;
    }

    public void setFaculties(SparseArray<String> faculties) {
        this.mFaculties = faculties;
    }

    public void setFake(boolean fake) {
        this.mFake = fake;
    }

    public boolean isFake() {
        return this.mFake;
    }




    public JSONObject getFacultiesJSON() {
        JSONObject obj = new JSONObject();
        JSONArray keys_array = getFacultiesKeysJSON();
        JSONArray vals_array = getFacultiesValuesJSON();

        try {
            obj.put(PREF_FACULTIES_KEYS, keys_array);
            obj.put(PREF_FACULTIES_VALUES, vals_array);
        } catch (JSONException e) {
            obj = new JSONObject();
        }

        return obj;
    }

    private JSONArray getFacultiesKeysJSON() {
        JSONArray array = new JSONArray();

        if (getFaculties()!= null) for (int i=0; i<getFaculties().size(); i++)
            array.put(getFaculties().keyAt(i));

        return array;
    }

    public String getFacultiesKeys() {
        return getFacultiesKeysJSON().toString();
    }

    private JSONArray getFacultiesValuesJSON() {
        JSONArray array = new JSONArray();

        if (getFaculties()!= null) for (int i=0; i<getFaculties().size(); i++)
            array.put(getFaculties().valueAt(i));

        return array;
    }

    public String getFacultiesValues() {
        return getFacultiesValuesJSON().toString();
    }

    @Ignore
    public boolean hasFacultiesList() {
        return getFaculties() != null && getFaculties().size() > 0;
    }

    @Ignore
    public void setFaculties(JSONObject obj) {
        ArrayList<Integer> keys = new ArrayList<>();
        ArrayList<String> vals = new ArrayList<>();

        try {
            if (obj.has(PREF_FACULTIES_KEYS)) {
                JSONArray keys_array = obj.getJSONArray(PREF_FACULTIES_KEYS);
                for (int i=0; i<keys_array.length(); i++)
                    keys.add(keys_array.getInt(i));
            }
            if (obj.has(PREF_FACULTIES_VALUES)) {
                JSONArray vals_array = obj.getJSONArray(PREF_FACULTIES_VALUES);
                for (int i=0; i<vals_array.length(); i++)
                    vals.add(vals_array.getString(i));
            }

            SparseArray<String> result = new SparseArray<>();
            for (int i=0; i<keys.size(); i++)
                result.put(keys.get(i), vals.get(i));

            setFaculties(result);

        } catch (JSONException e) {
            setFaculties(new SparseArray<>());
        }
    }

    @Ignore
    public String getSelectedFacultyUrl() {
        if (mSelectedFaculty != -1)
            return S3Helper.URL_CAREER_BASE +
                    "jsessionid=" + getSessionID() +
                    "?stu_id=" + mSelectedFaculty;
        else
            return null;
    }

    @Ignore
    public String getSelectedFacultyName() {
        return getFaculties().get(mSelectedFaculty);
    }

    public void setSelectedFaculty(int selectedFaculty) {
        this.mSelectedFaculty = selectedFaculty;
    }

    public int getSelectedFaculty() { return this.mSelectedFaculty; }

    @Ignore
    public boolean shouldChooseFaculty() { return !isFacultyChosen() && hasMultiFaculty(); }

    @Ignore
    private boolean hasMultiFaculty() { return hasFacultiesList(); }

    @Ignore
    public boolean hasOneFaculty() {
        return getFaculties() != null && getFaculties().size() == 0;
    }

    @Ignore
    public boolean isFacultyChosen() {
        return mSelectedFaculty != FACULTY_NOT_CHOSEN;
    }

}
