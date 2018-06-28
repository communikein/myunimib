package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import javax.inject.Singleton;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "user")
@Singleton
public class User extends UserAuthentication {

    public static final String PREFERENCES_USER = "user_details";
    public static final String PROFILE_PICTURE_PATH = "profile-picture";
    public static final String PROFILE_PICTURE_EXT = ".png";

    public static final String PREF_MATRICOLA = "user_matricola";
    public static final String PREF_NAME = "user_name";
    public static final String PREF_AVERAGE_MARK = "user_average_mark";
    public static final String PREF_TOTAL_CFU = "user_total_cfu";
    public static final String PREF_TAG = "user_tag";

    public static final float ERROR_AVERAGE_MARK = -1;
    public static final int ERROR_TOTAL_CFU = -1;

    private String realName;
    private String matricola;
    private float averageMark;
    private int totalCfu;
    @Ignore
    private Object mTag;

    @Ignore
    public User(String username, String password){
        super(username, password, null, null, null, false);

        setMatricola(null);
        setRealName(null);
        setAverageMark(ERROR_AVERAGE_MARK);
        setTotalCfu(ERROR_TOTAL_CFU);
    }

    @Ignore
    public User(@NonNull String username, String password, String realName, float averageMark,
                int totalCfu, String matricola) {
        super(username, password, null, null, null, false);

        setRealName(realName);
        setMatricola(matricola);
        setTotalCfu(totalCfu);
        setAverageMark(averageMark);
    }

    @Ignore
    public User(@NonNull String username, String sessionId, ArrayList<Faculty> faculties,
                Faculty selectedFaculty, String realName, String matricola, float averageMark,
                int totalCfu, boolean fake) {
        super(username, null, sessionId, faculties, selectedFaculty, fake);

        setRealName(realName);
        setMatricola(matricola);
        setAverageMark(averageMark);
        setTotalCfu(totalCfu);
    }

    public User(@NonNull String username, String realName, String matricola, float averageMark,
                int totalCfu, boolean fake) {
        super(username, null, "", new ArrayList<>(), null, fake);

        setRealName(realName);
        setMatricola(matricola);
        setAverageMark(averageMark);
        setTotalCfu(totalCfu);
    }


    public float getAverageMark() {
        return averageMark;
    }

    public void setAverageMark(float averageMark) {
        this.averageMark = averageMark;
    }

    public int getTotalCfu() {
        return totalCfu;
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
