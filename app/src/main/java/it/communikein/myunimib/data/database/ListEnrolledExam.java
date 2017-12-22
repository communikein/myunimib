package it.communikein.myunimib.data.database;

import java.util.Date;

public class ListEnrolledExam {

    private int adsceId;
    private String name;
    private Date date;
    private String description;

    public ListEnrolledExam(int adsceId, String name, Date date, String description) {
        this.adsceId = adsceId;
        this.name = name;
        this.date = date;
        this.description = description;
    }

    public int getAdsceId() {
        return adsceId;
    }

    public void setAdsceId(int adsceId) {
        this.adsceId = adsceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
