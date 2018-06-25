package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.data.network.loaders.S3Helper;

@SuppressWarnings({"WeakerAccess", "unused"})
public class UserAuthentication {

    public static final String PREF_USERNAME = "user_username";
    public static final String PREF_PASSWORD = "user_password";
    public static final String PREF_SESSION_ID = "user_session_id";
    public static final String PREF_FACULTIES = "user_faculties";
    private static final String PREF_FACULTIES_KEYS = "user_faculties_keys";
    private static final String PREF_FACULTIES_VALUES = "user_faculties_values";
    public static final String PREF_SELECTED_FACULTY = "selected_faculty";
    public static final String PREF_FAKE = "user_fake";


    @PrimaryKey
    @NonNull
    private String username;

    @Ignore // Stored in Account Manager
    private String password;

    @Ignore // Computed from username and password
    private String authToken;

    @Ignore // Stored in SharedPreferences
    private String sessionId;

    @Ignore
    private ArrayList<Faculty> faculties;

    @Ignore // Stored in SharedPreferences
    private Faculty selectedFaculty;

    private boolean fake;

    public UserAuthentication(String username, boolean fake) {
        setUsername(username);
        setPassword("");
        setAuthToken("", "");
        setSessionId("");
        setFaculties(new ArrayList<>());
        setSelectedFaculty(null);
        setFake(fake);
    }

    @Ignore
    UserAuthentication(String username, String password, String sessionID,
                       ArrayList<Faculty> faculties, Faculty selectedFaculty,
                       boolean isFake) {
        setUsername(username);
        setPassword(password);
        setAuthToken(username, password);
        setSessionId(sessionID);
        setFaculties(faculties);
        setSelectedFaculty(selectedFaculty);
        setFake(isFake);
    }



    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null) this.username = username;
        else this.username = "";
    }

    @Ignore
    public String getPassword() {
        return password;
    }

    @Ignore
    public void setPassword(String password) {
        if (password != null) {
            this.password = password;
            setAuthToken(getUsername(), password);
        }
        else this.password = "";
    }

    @Ignore
    public String getAuthToken() {
        return authToken;
    }

    @Ignore
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Ignore
    public boolean isAuthTokenSet() {
        return !TextUtils.isEmpty(getAuthToken());
    }

    @Ignore
    private void setAuthToken(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) this.authToken = null;

        String authString = username + ":" + password;
        byte[] authEncBytes = Base64.encode(authString.getBytes(), Base64.NO_WRAP);
        this.authToken = new String(authEncBytes);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionID) {
        if (sessionID != null) this.sessionId = sessionID;
        else this.sessionId = "";
    }

    @Ignore
    public ArrayList<Faculty> getFaculties() {
        return faculties;
    }

    @Ignore
    public void setFaculties(ArrayList<Faculty> faculties) {
        this.faculties = faculties;
    }

    @Ignore
    public void setFacultiesFromJson(String json) {
        Type collectionType = new TypeToken<ArrayList<Faculty>>(){}.getType();
        this.faculties = new Gson().fromJson(json, collectionType);
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public boolean isFake() {
        return this.fake;
    }

    public void setSelectedFaculty(Faculty selectedFaculty) {
        this.selectedFaculty = selectedFaculty;
    }

    public Faculty getSelectedFaculty() { return this.selectedFaculty; }



    @Ignore
    public String getFacultiesJSON() {
        return new Gson().toJson(getFaculties());
    }

    @Ignore
    private String getFacultiesCodes() {
        ArrayList<Integer> result = new ArrayList<>();

        if (getFaculties()!= null) for (Faculty faculty : getFaculties())
            result.add(faculty.getCode());

        return new Gson().toJson(result);
    }

    @Ignore
    private String getFacultiesValues() {
        ArrayList<String> result = new ArrayList<>();

        if (getFaculties()!= null) for (Faculty faculty : getFaculties())
            result.add(faculty.getName());

        return new Gson().toJson(result);
    }

    @Ignore
    private boolean hasFacultiesList() {
        return getFaculties() != null && getFaculties().size() > 0;
    }

    @Ignore
    public void setFaculties(String source) {
        Type collectionType = new TypeToken<ArrayList<Faculty>>(){}.getType();
        this.faculties = new Gson().fromJson(source, collectionType);
    }

    @Ignore
    public String getSelectedFacultyUrl() {
        if (isFacultyChosen())
            return S3Helper.URL_CAREER_BASE +
                    "jsessionid=" + getSessionId() +
                    "?stu_id=" + selectedFaculty.getCode();
        else
            return null;
    }

    @Ignore
    public boolean shouldChooseFaculty() { return !isFacultyChosen() && hasMultiFaculty(); }

    @Ignore
    public boolean hasMultiFaculty() { return hasFacultiesList(); }

    @Ignore
    public boolean hasOneFaculty() {
        return getFaculties() != null && getFaculties().size() == 0;
    }

    @Ignore
    public boolean isFacultyChosen() {
        return selectedFaculty != null;
    }

}
