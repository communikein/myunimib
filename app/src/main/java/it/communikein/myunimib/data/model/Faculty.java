package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "faculties", foreignKeys = @ForeignKey(entity = User.class,
            parentColumns = "username",
            childColumns = "userId",
            onDelete = CASCADE))
public class Faculty implements Parcelable {

    public static final String FACULTY_NAME = "faculty_name";
    public static final String FACULTY_CODE = "faculty_code";
    public static final String FACULTY_USER = "faculty_user";

    @SerializedName(FACULTY_NAME)
    private String name;

    @SerializedName(FACULTY_CODE)
    @PrimaryKey @NonNull
    private int code;

    @SerializedName(FACULTY_USER)
    private String userId;

    public Faculty(String name, int code, String userId) {
        this.name = name;
        this.code = code;
        this.userId = userId;
    }

    @Ignore
    public Faculty(String faculty) {
        Faculty temp = new Gson().fromJson(faculty, Faculty.class);
        this.setName(temp.getName());
        this.setCode(temp.getCode());
    }

    public static Faculty fromParcel(Parcel origin) {
        return new Faculty(origin.readString(), origin.readInt(), origin.readString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String user) {
        this.userId = user;
    }



    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getName());
        dest.writeInt(getCode());
        dest.writeString(getUserId());
    }

    public static final Parcelable.Creator<Faculty> CREATOR = new Parcelable.Creator<Faculty>() {

        public Faculty createFromParcel(Parcel in) {
            return Faculty.fromParcel(in);
        }

        public Faculty[] newArray(int size) {
            return new Faculty[size];
        }
    };


    public static String toJson(ArrayList<Faculty> faculties) {
        return new Gson().toJson(faculties);
    }

    public static ArrayList<Faculty> fromJson(String faculties) {
        Type collectionType = new TypeToken<ArrayList<Faculty>>(){}.getType();
        return new Gson().fromJson(faculties, collectionType);
    }
}
