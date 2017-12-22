package it.communikein.myunimib.data.database;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.os.Environment;

import it.communikein.myunimib.utilities.MyunimibDateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

@Entity(tableName = "enrolled_exams", indices = {@Index(value = {"adsceId"}, unique = true)})
public class EnrolledExam extends Exam {

    private static final String ARG_CODE = "ARG_CODE";
    private static final String ARG_BUILDING = "ARG_BUILDING";
    private static final String ARG_ROOM = "ARG_ROOM";
    private static final String ARG_RESERVED = "ARG_RESERVED";
    private static final String ARG_TEACHERS = "ARG_TEACHERS";

    private String code;
    private String building;
    private String room;
    private String reserved;
    private ArrayList<String> teachers;


    public EnrolledExam(int cdsEsaId, int attDidEsaId, int appId, int adsceId, String name,
                        Date date, String description, String code, String building, String room,
                        String reserved, ArrayList<String> teachers) {
        super(cdsEsaId, attDidEsaId, appId, adsceId, name, date, description);

        this.code = code;
        this.building = building;
        this.room = room;
        this.reserved = reserved;
        this.teachers = teachers;
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

    @Ignore
    public EnrolledExam(JSONObject obj) throws JSONException, NullPointerException, ParseException {
        super(obj);

        if (obj.has(ARG_CODE))
            setCode(obj.getString(ARG_CODE));
        if (obj.has(ARG_BUILDING))
            setBuilding(obj.getString(ARG_BUILDING));
        if (obj.has(ARG_ROOM))
            setRoom(obj.getString(ARG_ROOM));
        if (obj.has(ARG_RESERVED))
            setReserved(obj.getString(ARG_RESERVED));
        if (obj.has(ARG_TEACHERS))
            setTeachers(obj.getJSONArray(ARG_TEACHERS));
    }


    public String getCode() { return this.code; }

    private void setCode(String code) {
        this.code = code;
    }

    public String getBuilding() {
        return this.building;
    }

    private void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return this.room;
    }

    private void setRoom(String room) {
        this.room = room;
    }

    public String getReserved() {
        return this.reserved;
    }

    private void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public ArrayList<String> getTeachers() {
        return this.teachers;
    }

    @Ignore
    private JSONArray getTeachersJson() {
        JSONArray array = new JSONArray();
        for (String teacher : teachers)
            array.put(teacher);

        return array;
    }

    public String printTeachers() {
        StringBuilder ris = new StringBuilder();
        for (String teacher : getTeachers())
            ris.append(teacher).append("\n");

        return ris.toString();
    }

    private void setTeachers(ArrayList<String> teachers) {
        this.teachers = teachers;
    }

    private void setTeachers(JSONArray array) {
        ArrayList<String> teachers = new ArrayList<>();

        try {
            for (int i = 0; i < array.length(); i++)
                teachers.add(array.getString(i));
        } catch (JSONException e) {
            teachers = new ArrayList<>();
        }

        setTeachers(teachers);
    }


    @Ignore
    private String getCertificateName(){
        return getName() + " - " + MyunimibDateUtils.dateFile.format(getDate()) + ".pdf";
    }

    @Ignore
    public String printLocation(){
        if (!getRoom().equals("")) {
            String ris = "Milano, ";

            try {
                ris = ris + getBuilding() + ", " + getRoom().substring(0, getRoom().indexOf("-"));
            } catch (Exception e) {
                ris = ris + getBuilding() + ", " + getRoom();
            }

            return ris;
        }
        else
            return "";
    }



    @Ignore
    public JSONObject toJSON() {
        JSONObject obj;

        try {
            obj = super.toJSON();

            obj.put(ARG_CODE, getCode());
            obj.put(ARG_BUILDING, getBuilding());
            obj.put(ARG_ROOM, getRoom());
            obj.put(ARG_RESERVED, getReserved());
            obj.put(ARG_TEACHERS, getTeachersJson());
        } catch (JSONException e){
            obj = new JSONObject();
        }

        return obj;
    }

    @Ignore
    @Override
    public String toString() {
        return toJSON().toString();
    }


    @Ignore
    public static File getCertificatePath(EnrolledExam exam) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                exam.getCertificateName());
    }
}
