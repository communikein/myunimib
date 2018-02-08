package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import java.util.ArrayList;
import java.util.Date;

@Entity(tableName = "enrolled_exams", primaryKeys = {"adsceId", "appId", "attDidEsaId", "cdsEsaId"})
public class EnrolledExam extends Exam {

    private String code;
    private String building;
    private String room;
    private String reserved;
    private ArrayList<String> teachers;


    public EnrolledExam(int cdsEsaId, int attDidEsaId, int appId, int adsceId, String name,
                        Date date, String description, String code, String building, String room,
                        String reserved, ArrayList<String> teachers) {
        super(cdsEsaId, attDidEsaId, appId, adsceId, name, date, description);

        setCode(code);
        setBuilding(building);
        setRoom(room);
        setReserved(reserved);
        setTeachers(teachers);
    }

    @Ignore
    public EnrolledExam(ExamID id, String name, Date date, String description, String code,
                        String building, String room, String reserved, ArrayList<String> teachers) {
        super(id, name, date, description);

        setCode(code);
        setBuilding(building);
        setRoom(room);
        setReserved(reserved);
        setTeachers(teachers);
    }


    public String getCode() { return this.code; }

    private void setCode(String code) {
        if (code == null) this.code = "";
        else this.code = code;
    }

    public String getBuilding() {
        return this.building;
    }

    private void setBuilding(String building) {
        if (building == null) this.building = "";
        else this.building = building;
    }

    public String getRoom() {
        return this.room;
    }

    private void setRoom(String room) {
        if (room == null) this.room = "";
        else this.room = room;
    }

    public String getReserved() {
        return this.reserved;
    }

    private void setReserved(String reserved) {
        if (reserved == null) this.reserved = "";
        else this.reserved = reserved;
    }

    public ArrayList<String> getTeachers() {
        return this.teachers;
    }

    public String printTeachers() {
        StringBuilder ris = new StringBuilder();
        for (String teacher : getTeachers())
            ris.append(teacher).append("\n");

        return ris.toString();
    }

    private void setTeachers(ArrayList<String> teachers) {
        if (teachers == null) this.teachers = new ArrayList<>();
        else this.teachers = teachers;
    }


    @Override
    public boolean isIdentic(Object obj) {
        if (! (obj instanceof EnrolledExam)) return false;

        EnrolledExam exam = (EnrolledExam) obj;
        return super.isIdentic(exam) &&
                exam.getBuilding().equals(getBuilding()) &&
                exam.getCode().equals(getCode()) &&
                exam.getReserved().equals(getReserved());
    }
}
