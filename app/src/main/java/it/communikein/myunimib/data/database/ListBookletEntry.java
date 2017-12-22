package it.communikein.myunimib.data.database;


public class ListBookletEntry {

    private int adsceId;
    private String name;
    private String state;
    private String score;

    public ListBookletEntry(int adsceId, String name, String state, String score) {
        setAdsceId(adsceId);
        setScore(score);
        setName(name);
        setState(state);
    }

    public int getAdsceId() {
        return adsceId;
    }

    private void setAdsceId(int adsceId) {
        this.adsceId = adsceId;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
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

}
