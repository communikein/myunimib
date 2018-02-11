package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "booklet")
public class BookletEntry {

    @PrimaryKey
    private int adsceId;
    private String name;
    private Date date;
    private String state;
    private String code;
    private int cfu;
    private String score;

    public BookletEntry(int adsceId, String name, Date date, int cfu, String state,
                        String score, String code) {
        setCfu(cfu);
        setScore(score);
        setName(name);
        setAdsceId(adsceId);
        setDate(date);
        setState(state);
        setCode(code);
    }


    public int getAdsceId() {
        return adsceId;
    }

    public void setAdsceId(int adsceId) {
        this.adsceId = adsceId;
    }

    public int getCfu() {
        return cfu;
    }

    public void setCfu(int cfu) {
        this.cfu = cfu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) this.name = "";
        else this.name = name;
    }

    public Date getDate() {
        return date;
    }

    @Ignore
    private long getMillis() {
        if (getDate() == null)
            return -1;
        else
            return getDate().getTime();
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(long millis) {
        if (millis < 0) setDate(null);
        else setDate(new Date(millis));
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null) this.code = "";
        else this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (state == null) this.state = "";
        else this.state = state;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        if (score == null) this.score = "";
        else this.score = score;
    }


    public boolean isPassed() {
        boolean passed = false;
        if (getState().toLowerCase().contains("superata"))
            passed = true;

        return passed;
    }


    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof BookletEntry)) return false;
        BookletEntry entry = (BookletEntry) obj;

        return getAdsceId() == entry.getAdsceId();
    }

    public boolean isIdentic(Object obj) {
        if (! (obj instanceof BookletEntry)) return false;
        BookletEntry entry = (BookletEntry) obj;

        return getAdsceId() == entry.getAdsceId() &&
                getState().equals(entry.getState()) &&
                getCfu() == entry.getCfu() &&
                getCode().equals(entry.getCode()) &&
                getMillis() == entry.getMillis() &&
                getName().equals(entry.getName()) &&
                getScore().equals(entry.getScore());
    }
}
