package com.communikein.myunimib.data.type;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class BookletEntry {

    private final static String ARG_NAME = "ARG_NAME";
    private final static String ARG_CFU = "ARG_CFU";
    private final static String ARG_SCORE = "ARG_SCORE";
    private final static String ARG_DATE = "ARG_DATE";
    private final static String ARG_STATE = "ARG_STATE";
    private final static String ARG_CODE = "ARG_CODE";
    private final static String ARG_ADSCE_ID = "ARG_ADSCE_ID";

    private String name;
    private Date date;
    private String state;
    private String code;
    private int adsce_id;
    private int cfu;
    private String score;

    public BookletEntry(int adsce_id, String name, Date date, int cfu, String state, String mark,
                        String code) {
        setCFU(cfu);
        setScore(mark);
        setName(name);
        setADSCE_ID(adsce_id);
        setDate(date);
        setState(state);
        setCode(code);
    }

    public BookletEntry(JSONObject json) throws JSONException {
        if (json == null) throw new IllegalArgumentException();

        if (json.has(ARG_CFU))
            setCFU(json.getInt(ARG_CFU));
        if (json.has(ARG_SCORE))
            setScore(json.getString(ARG_SCORE));
        if (json.has(ARG_NAME))
            setName(json.getString(ARG_NAME));
        if (json.has(ARG_STATE))
            setState(json.getString(ARG_STATE));
        if (json.has(ARG_CODE))
            setCode(json.getString(ARG_CODE));
        if (json.has(ARG_ADSCE_ID))
            setADSCE_ID(json.getInt(ARG_ADSCE_ID));
        if (json.has(ARG_DATE)) {
            long millis = json.getLong(ARG_DATE);

            if (millis == 0)
                setDate(null);
            else
                setDate(new Date(json.getLong(ARG_DATE)));
        }
    }

    public int getADSCE_ID() {
        return adsce_id;
    }

    private void setADSCE_ID(int adsce_id) {
        this.adsce_id = adsce_id;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    private Date getDate() {
        return date;
    }

    public long getMillis() {
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

    public int getCfu() {
        return cfu;
    }

    private void setCFU(int cfu) {
        this.cfu = cfu;
    }

    public String getScore() {
        return score;
    }

    private void setScore(String score) {
        this.score = score;
    }


    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put(ARG_CFU, getCfu());
            json.put(ARG_SCORE, getScore());
            json.put(ARG_CFU, getCfu());
            json.put(ARG_SCORE, getScore());
            json.put(ARG_NAME, getName());
            json.put(ARG_ADSCE_ID, getADSCE_ID());
            json.put(ARG_CODE, getCode());
            json.put(ARG_DATE, getMillis());
            json.put(ARG_STATE, getState());
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
            return null;
        }

        return json;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

}
