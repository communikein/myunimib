package it.communikein.myunimib.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import javax.inject.Singleton;

@SuppressWarnings("unused")
@Entity(tableName = "user")
@Singleton
public class User extends UserAuthentication {

    public static final String PREFERENCES_USER = "user_details";
    public static final String SAVED_PROFILE_PICTURE = "/profile_picture.png";

    public static final String PREF_MATRICOLA = "user_matricola";
    public static final String PREF_NAME = "user_name";
    public static final String PREF_AVERAGE_MARK = "user_average_mark";
    public static final String PREF_TOTAL_CFU = "user_total_cfu";
    public static final String PREF_TAG = "user_tag";

    public static final float ERROR_AVERAGE_MARK = -1;
    public static final int ERROR_TOTAL_CFU = -1;

    @ColumnInfo(name = "name")
    private String mName;
    @ColumnInfo(name = "matricola")
    private String mMatricola;
    @ColumnInfo(name = "averageMark")
    private float mAverageMark;
    @ColumnInfo(name = "totalCFU")
    private int mTotalCFU;
    @Ignore
    private Object mTag;

    @Ignore
    public User(String username, String password){
        super(username, password, null, null, -1, false);

        setMatricola(null);
        setName(null);
        setAverageMark(ERROR_AVERAGE_MARK);
        setTotalCFU(ERROR_TOTAL_CFU);
    }

    @Ignore
    public User(@NonNull String username, String password, String name, float averageMark,
                int totalCFU, String matricola) {
        super(username, password, null, null, -1, false);

        setName(name);
        setMatricola(matricola);
        setTotalCFU(totalCFU);
        setAverageMark(averageMark);
    }

    public User(@NonNull String username, String sessionID, SparseArray<String> faculties,
                int selectedFaculty, String name, String matricola, float averageMark,
                int totalCFU, boolean fake) {
        super(username, null, sessionID, faculties, selectedFaculty, fake);

        setName(name);
        setMatricola(matricola);
        setAverageMark(averageMark);
        setTotalCFU(totalCFU);
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
