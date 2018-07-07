package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "booklet", indices = {@Index(value = "adsceId", unique = true)})
public class BookletEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int adsceId;
    private String name;
    private Date date;
    private String state;
    private String code;
    private int cfu;
    private String score;
    private boolean fake;

    public BookletEntry(int id, int adsceId, String name, Date date, int cfu, String state,
                        String score, String code, boolean fake) {
        setId(id);
        setCfu(cfu);
        setScore(score);
        setName(name);
        setAdsceId(adsceId);
        setDate(date);
        setState(state);
        setCode(code);
        setFake(fake);
    }

    @Ignore
    public BookletEntry(int adsceId, String name, Date date, int cfu, String state,
                        String score, String code, boolean fake) {
        setCfu(cfu);
        setScore(score);
        setName(name);
        setAdsceId(adsceId);
        setDate(date);
        setState(state);
        setCode(code);
        setFake(fake);
    }

    @Ignore
    public BookletEntry(String name, int cfu, String score) {
        setCfu(cfu);
        setScore(score);
        setName(name);
        setDate(new Date());
        setState("");
        setCode("");
        setFake(true);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Ignore
    public int getScoreValue() {
        try {
            return Integer.parseInt(getScore());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setScore(String score) {
        if (score == null) this.score = "";
        else this.score = score;
    }

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public boolean isPassed() {
        boolean passed = false;
        if (getState().toLowerCase().contains("superata"))
            passed = true;

        return passed;
    }


    @Override
    @Ignore
    public boolean equals(Object obj) {
        if (! (obj instanceof BookletEntry)) return false;
        BookletEntry entry = (BookletEntry) obj;

        return this.getId() == entry.getId();
    }

    @Ignore
    public boolean isIdentic(Object obj) {
        if (! (obj instanceof BookletEntry)) return false;
        BookletEntry entry = (BookletEntry) obj;

        return this.getAdsceId() == entry.getAdsceId() &&
                this.getState().equals(entry.getState()) &&
                this.getCfu() == entry.getCfu() &&
                this.getCode().equals(entry.getCode()) &&
                this.getMillis() == entry.getMillis() &&
                this.getName().equals(entry.getName()) &&
                this.getScore().equals(entry.getScore());
    }

    @Ignore
    public boolean displayEquals(Object obj) {
        if (! (obj instanceof BookletEntry)) return false;
        BookletEntry entry = (BookletEntry) obj;

        return this.getName().equals(entry.getName()) &&
                this.getScore().equals(entry.getScore()) &&
                this.getState().equals(entry.getState());
    }
}
