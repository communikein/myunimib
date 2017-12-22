package it.communikein.myunimib.data.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

@Entity(tableName = "booklet", indices = {@Index(value = {"adsceId"}, unique = true)})
public class BookletEntry {

    private final static String ARG_NAME = "ARG_NAME";
    private final static String ARG_CFU = "ARG_CFU";
    private final static String ARG_SCORE = "ARG_SCORE";
    private final static String ARG_DATE = "ARG_DATE";
    private final static String ARG_STATE = "ARG_STATE";
    private final static String ARG_CODE = "ARG_CODE";
    private final static String ARG_ADSCE_ID = "ARG_ADSCE_ID";

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private Date date;
    private String state;
    private String code;
    private int adsceId;
    private int cfu;
    private String score;

    @Ignore
    public BookletEntry(int adsce_id, String name, Date date, int cfu, String state, String mark,
                        String code) {
        setCfu(cfu);
        setScore(mark);
        setName(name);
        setAdsceId(adsce_id);
        setDate(date);
        setState(state);
        setCode(code);
    }

    public BookletEntry(int id, int adsceId, String name, Date date, int cfu, String state,
                        String score, String code) {
        setId(id);
        setCfu(cfu);
        setScore(score);
        setName(name);
        setAdsceId(adsceId);
        setDate(date);
        setState(state);
        setCode(code);
    }

    @Ignore
    public BookletEntry(JSONObject json) throws JSONException {
        if (json == null) throw new IllegalArgumentException();

        if (json.has(ARG_CFU))
            setCfu(json.getInt(ARG_CFU));
        if (json.has(ARG_SCORE))
            setScore(json.getString(ARG_SCORE));
        if (json.has(ARG_NAME))
            setName(json.getString(ARG_NAME));
        if (json.has(ARG_STATE))
            setState(json.getString(ARG_STATE));
        if (json.has(ARG_CODE))
            setCode(json.getString(ARG_CODE));
        if (json.has(ARG_ADSCE_ID))
            setAdsceId(json.getInt(ARG_ADSCE_ID));
        if (json.has(ARG_DATE)) {
            long millis = json.getLong(ARG_DATE);

            if (millis == 0)
                setDate(null);
            else
                setDate(new Date(json.getLong(ARG_DATE)));
        }
    }

    public int getId() { return this.id; }

    private void setId(int id) {
        this.id = id;
    }

    public int getAdsceId() {
        return adsceId;
    }

    private void setAdsceId(int adsceId) {
        this.adsceId = adsceId;
    }

    public int getCfu() {
        return cfu;
    }

    private void setCfu(int cfu) {
        this.cfu = cfu;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    @Ignore
    private long getMillis() {
        if (getDate() == null)
            return 0;
        else
            return getDate().getTime();
    }

    private void setDate(Date date) {
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    private void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    private void setState(String state) {
        this.state = state;
    }

    public String getScore() {
        return score;
    }

    private void setScore(String score) {
        this.score = score;
    }

    @Ignore
    private JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put(ARG_CFU, getCfu());
            json.put(ARG_SCORE, getScore());
            json.put(ARG_CFU, getCfu());
            json.put(ARG_SCORE, getScore());
            json.put(ARG_NAME, getName());
            json.put(ARG_ADSCE_ID, getAdsceId());
            json.put(ARG_CODE, getCode());
            json.put(ARG_DATE, getMillis());
            json.put(ARG_STATE, getState());
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
            return null;
        }

        return json;
    }

    @Ignore
    @Override
    public String toString() {
        return toJSON().toString();
    }

}
