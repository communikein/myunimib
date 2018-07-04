package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import javax.inject.Singleton;

import it.communikein.myunimib.utilities.Utils;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "user")
@Singleton
public class User extends UserAuthentication {

    public static final String PREFERENCES_USER = "user_details";
    public static final String PROFILE_PICTURE_PATH = "profile-picture";
    public static final String PROFILE_PICTURE_EXT = ".png";

    public static final String PREF_MATRICOLA = "user_matricola";
    public static final String PREF_NAME = "user_name";
    public static final String PREF_AVERAGE_SCORE = "user_average_score";
    public static final String PREF_TOTAL_CFU = "user_total_cfu";
    public static final String PREF_TAG = "user_tag";

    public static final float ERROR_AVERAGE_MARK = -1;
    public static final int ERROR_TOTAL_CFU = -1;

    private String realName;
    private String matricola;
    private float averageScore;
    private int totalCfu;
    @Ignore
    private Object mTag;

    @Ignore
    public User(String username, String password){
        super(username, password, null, null, null, false);

        setMatricola(null);
        setRealName(null);
        setAverageScore(ERROR_AVERAGE_MARK);
        setTotalCfu(ERROR_TOTAL_CFU);
    }

    @Ignore
    public User(@NonNull String username, String password, String realName, float averageScore,
                int totalCfu, String matricola) {
        super(username, password, null, null, null, false);

        setRealName(realName);
        setMatricola(matricola);
        setTotalCfu(totalCfu);
        setAverageScore(averageScore);
    }

    @Ignore
    public User(@NonNull String username, String sessionId, ArrayList<Faculty> faculties,
                Faculty selectedFaculty, String realName, String matricola, float averageScore,
                int totalCfu, boolean fake) {
        super(username, null, sessionId, faculties, selectedFaculty, fake);

        setRealName(realName);
        setMatricola(matricola);
        setAverageScore(averageScore);
        setTotalCfu(totalCfu);
    }

    public User(@NonNull String username, String realName, String matricola, float averageScore,
                int totalCfu, boolean fake) {
        super(username, null, "", new ArrayList<>(), null, fake);

        setRealName(realName);
        setMatricola(matricola);
        setAverageScore(averageScore);
        setTotalCfu(totalCfu);
    }


    public float getAverageScore() {
        return averageScore;
    }

    public String printAverageScore() {
        if (getAverageScore() < 0) return "-";
        return Utils.markFormat.format(getAverageScore());
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public int getTotalCfu() {
        return totalCfu;
    }

    public String printTotalCfu() {
        if (getTotalCfu() < 0) return "-";
        return String.valueOf(getTotalCfu());
    }

    public void setTotalCfu(int totalCFU) {
        this.totalCfu = totalCFU;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String name) {
        if (name != null) this.realName = name;
        else this.realName = "";
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        if (matricola != null) this.matricola = matricola;
        else this.matricola = "";
    }

    @Ignore
    public String getUniversityMail(){
        return getUsername() + "@campus.unimib.it";
    }



    @Ignore
    public Object getTag() {
        return mTag;
    }

    @Ignore
    public void setTag(Object tag) {
        this.mTag = tag;
    }
}
