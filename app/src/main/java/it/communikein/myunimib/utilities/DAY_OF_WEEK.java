package it.communikein.myunimib.utilities;

public enum DAY_OF_WEEK {
    MONDAY("MONDAY"),
    TUESDAY("TUESDAY"),
    WEDNESDAY("WEDNESDAY"),
    THURSDAY("THURSDAY"),
    FRIDAY("FRIDAY"),
    SATURDAY("SATURDAY"),
    SUNDAY("SUNDAY");

    private final String day;

    DAY_OF_WEEK(String day) {
        this.day = day;
    }

    public String getDay() {
        return this.day;
    }
}
