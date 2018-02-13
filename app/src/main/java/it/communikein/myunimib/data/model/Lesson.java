package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import it.communikein.myunimib.utilities.DateHelper;
import it.communikein.myunimib.utilities.Utils;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "lessons", indices = {
        @Index(value = {"courseName", "dayOfWeek", "timeStart", "timeEnd"}, unique = true)})
public class Lesson {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    private String courseName;
    @NonNull
    private String dayOfWeek;
    private long timeStart;
    private long timeEnd;
    private String building;
    private String classroom;

    public Lesson(int id, String courseName, String building, String classroom, String dayOfWeek,
                  long timeStart, long timeEnd) {
        setId(id);
        setCourseName(courseName);
        setBuilding(building);
        setClassroom(classroom);
        setDayOfWeek(dayOfWeek);
        setTimeEnd(timeEnd);
        setTimeStart(timeStart);
    }

    @Ignore
    public Lesson(String courseName, String building, String classroom, String dayOfWeek,
                  long timeStart, long timeEnd) {
        setCourseName(courseName);
        setBuilding(building);
        setClassroom(classroom);
        setDayOfWeek(dayOfWeek);
        setTimeEnd(timeEnd);
        setTimeStart(timeStart);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(@NonNull String day_of_week) {
        this.dayOfWeek = day_of_week;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    @Ignore
    public String printTimeEnd() {
        return DateHelper.printTime(getTimeEnd());
    }

    public void setTimeEnd(long time_end) {
        this.timeEnd = time_end;
    }

    public long getTimeStart() {
        return timeStart;
    }

    @Ignore
    public String printTimeStart() {
        return DateHelper.printTime(getTimeStart());
    }

    public void setTimeStart(long time_start) {
        this.timeStart = time_start;
    }

    @NonNull public String getCourseName() {
        return courseName;
    }

    public void setCourseName(@NonNull String course_name) {
        this.courseName = course_name;
    }

    public String getClassroom() {
        if (Utils.isInteger(classroom) && classroom.length() == 1)
            return "0" + classroom;
        else return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    @Ignore
    public String getBuildingClass(){
        return getBuilding() + " - " + getClassroom();
    }




    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Lesson)) return false;

        Lesson lesson = (Lesson) obj;
        return lesson.getCourseName().equals(this.getCourseName()) &&
                lesson.getDayOfWeek().equals(this.getDayOfWeek()) &&
                lesson.getTimeStart() == this.getTimeStart() &&
                lesson.getTimeEnd() == this.getTimeEnd();
    }

    public boolean displayEquals(Object obj) {
        Lesson lesson = (Lesson) obj;
        return lesson.equals(this) &&
                lesson.getBuilding().equals(this.getBuilding()) &&
                lesson.getClassroom().equals(this.getClassroom());
    }
}
