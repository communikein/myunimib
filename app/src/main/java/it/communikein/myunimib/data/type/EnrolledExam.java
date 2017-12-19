package it.communikein.myunimib.data.type;


import android.os.Environment;

import it.communikein.myunimib.utilities.MyunimibDateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;


public class EnrolledExam extends Exam {

    private static final String ARG_CODE = "ARG_CODE";
    private static final String ARG_BUILDING = "ARG_BUILDING";
    private static final String ARG_ROOM = "ARG_ROOM";
    private static final String ARG_RESERVED = "ARG_RESERVED";
    private static final String ARG_TEACHERS = "ARG_TEACHERS";

    private String mCode;
    private String mBuilding;
    private String mRoom;
    private String mReserved;
    private ArrayList<String> mTeachers;


    public EnrolledExam(ExamID id, String name, Date date, String description, String code,
                        String building, String room, String reserved, ArrayList<String> teachers) {
        super(id, name, date, description);

        setCode(code);
        setBuilding(building);
        setRoom(room);
        setReserved(reserved);
        setTeachers(teachers);
    }

    public EnrolledExam(JSONObject obj) throws JSONException, NullPointerException, ParseException {
        super(obj);

        if (obj == null) throw new NullPointerException();

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

    public String getCode() { return this.mCode; }

    private void setCode(String code) {
        this.mCode = code;
    }

    public String getBuilding() {
        return this.mBuilding;
    }

    private void setBuilding(String building) {
        this.mBuilding = building;
    }

    public String getRoom() {
        return this.mRoom;
    }

    private void setRoom(String room) {
        this.mRoom = room;
    }

    public String getReserved() {
        return this.mReserved;
    }

    private void setReserved(String reserved) {
        this.mReserved = reserved;
    }

    public ArrayList<String> getTeachers() {
        return this.mTeachers;
    }

    public JSONArray getTeachersJson() {
        JSONArray array = new JSONArray();
        for (String teacher : mTeachers)
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
        this.mTeachers = teachers;
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

    public String getCertificateName(){
        return getName() + " - " + MyunimibDateUtils.dateFile.format(getDate()) + ".pdf";
    }

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

    @Override
    public String toString() {
        return toJSON().toString();
    }


    public static File getCertificatePath(EnrolledExam exam) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                exam.getCertificateName());
    }
}
