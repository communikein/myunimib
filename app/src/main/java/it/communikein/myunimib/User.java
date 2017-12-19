package it.communikein.myunimib;

import android.util.Base64;
import android.util.SparseArray;

import it.communikein.myunimib.sync.S3Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class User {

    public static final String PREFERENCES_USER = "user_details";
    public static final String SAVED_PROFILE_PICTURE = "/profile_picture.png";

    public static final String PREF_USERNAME = "user_username";
    public static final String PREF_PASSWORD = "user_password";
    public static final String PREF_AUTH_TOKEN = "user_auth_token";
    public static final String PREF_SESSION_ID = "user_session_id";
    public static final String PREF_FACULTIES = "user_faculties";
    public static final String PREF_FACULTIES_KEYS = "user_faculties_keys";
    public static final String PREF_FACULTIES_VALUES = "user_faculties_values";
    public static final String PREF_SELECTED_FACULTY = "selected_faculty";
    public static final String PREF_MATRICOLA = "user_matricola";
    public static final String PREF_NAME = "user_name";
    public static final String PREF_AVERAGE_MARK = "user_average_mark";
    public static final String PREF_TOTAL_CFU = "user_total_cfu";
    public static final String PREF_TAG = "user_tag";
    public static final String PREF_FAKE = "user_fake";

    public static final float ERROR_AVERAGE_MARK = -1;
    public static final int ERROR_TOTAL_CFU = -1;

    public static final int FACULTY_NOT_CHOSEN = -1;

    private String mUsername;
    private String mPassword;
    private String mAuthToken;
    private String mSessionId;
    private SparseArray<String> mFaculties;
    private int mSelectedFaculty;
    private String mName;
    private String mMatricola;
    private float mAverageMark;
    private int mTotalCFU;
    private Object mTag;
    private boolean mFake;

    public User(String username, String password){
        setUsername(username);
        setPassword(password);
        setAuthToken(username, password);
        setSessionID(null);
        setFaculties((SparseArray<String>) null);
        setSelectedFaculty(-1);
        setMatricola(null);
        setName(null);
        setAverageMark(ERROR_AVERAGE_MARK);
        setTotalCFU(ERROR_TOTAL_CFU);
    }

    public User(String username, String password, String name, float averageMark, int totalCFU,
                    String matricola) {
        setName(name);
        setMatricola(matricola);
        setPassword(password);
        setUsername(username);
        setAuthToken(username, password);
        setSessionID(null);
        setTotalCFU(totalCFU);
        setAverageMark(averageMark);
        setFaculties((SparseArray<String>) null);
        setSelectedFaculty(-1);
    }


    public String getUsername() {
        return mUsername;
    }

    private void setUsername(String username) {
        if (username != null) this.mUsername = username;
        else this.mUsername = "";
    }

    public String getPassword() {
        return mPassword;
    }

    private void setPassword(String password) {
        if (password != null) this.mPassword = password;
        else this.mPassword = "";
    }

    public float getAverageMark() {
        return mAverageMark;
    }

    public void setAverageMark(float averageMark) {
        this.mAverageMark = averageMark;
    }

    public int getTotalCFU() {
        return mTotalCFU;
    }

    public void setTotalCFU(int totalCFU) {
        this.mTotalCFU = totalCFU;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        if (name != null) this.mName = name;
        else this.mName = "";
    }

    public String getMatricola() {
        return mMatricola;
    }

    public void setMatricola(String matricola) {
        if (matricola != null) this.mMatricola = matricola;
        else this.mMatricola = "";
    }

    public String getUniversityMail(){
        return mUsername + "@campus.unimib.it";
    }

    public void setFake(boolean fake) {
        this.mFake = fake;
    }

    public boolean isFake() {
        return this.mFake;
    }



    public SparseArray<String> getFaculties() {
        return mFaculties;
    }

    public JSONObject getFacultiesJSON() {
        JSONObject obj = new JSONObject();

        JSONArray keys_array = new JSONArray();
        JSONArray vals_array = new JSONArray();

        for (int i=0; i<getFaculties().size(); i++) {
            keys_array.put(getFaculties().keyAt(i));
            vals_array.put(getFaculties().valueAt(i));
        }

        try {
            obj.put(PREF_FACULTIES_KEYS, keys_array);
            obj.put(PREF_FACULTIES_VALUES, vals_array);
        } catch (JSONException e) {
            obj = null;
        }

        return obj;
    }

    public boolean hasFacultiesList() {
        return getFaculties() != null && getFaculties().size() > 0;
    }

    public void setFaculties(SparseArray<String> courses) {
        this.mFaculties = courses;
    }

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

            SparseArray<String> faculties = new SparseArray<>();
            for (int i=0; i<keys.size(); i++)
                faculties.put(keys.get(i), vals.get(i));

            setFaculties(faculties);

        } catch (JSONException e) {
            setFaculties((SparseArray<String>) null);
        }
    }

    public String getSelectedFacultyUrl() {
        if (mSelectedFaculty != -1)
            return S3Helper.URL_CAREER_BASE +
                    "jsessionid=" + getSessionID() +
                    "?stu_id=" + mSelectedFaculty;
        else
            return null;
    }

    public String getSelectedFacultyName() {
        return getFaculties().get(mSelectedFaculty);
    }

    public void setSelectedFaculty(int selectedFaculty) {
        this.mSelectedFaculty = selectedFaculty;
    }

    public int getSelectedFaculty() { return this.mSelectedFaculty; }

    public boolean shouldChooseFaculty() {
        return !isFacultyChosen() && hasMultiFaculty();
    }

    private boolean hasMultiFaculty() {
        return hasFacultiesList();
    }

    public boolean hasOneFaculty() {
        return getFaculties() != null && getFaculties().size() == 0;
    }

    public boolean isFacultyChosen() {
        return mSelectedFaculty != FACULTY_NOT_CHOSEN;
    }



    private void setAuthToken(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) this.mAuthToken = null;

        String authString = username + ":" + password;
        byte[] authEncBytes = Base64.encode(authString.getBytes(), Base64.NO_WRAP);
        this.mAuthToken = new String(authEncBytes);
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public String getSessionID() {
        return mSessionId;
    }

    public void setSessionID(String sessionID) {
        if (sessionID != null) this.mSessionId = sessionID;
        else this.mSessionId = "";
    }

    public Object getTag() {
        return mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }
}
