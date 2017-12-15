package com.communikein.myunimib.data.type;

import com.communikein.myunimib.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by eliam on 12/5/2017.
 */

public class Exam {

    public static final String EXAM = "EXAM";

    public static final String ARG_NAME = "ARG_NAME";
    public static final String ARG_DATE = "ARG_DATE";
    public static final String ARG_DESCRIPTION = "ARG_DESCRIPTION";
    public static final String ARG_BUILDING = "ARG_BUILDING";
    public static final String ARG_ROOM = "ARG_ROOM";

    protected ExamID id;
    private String name;
    private Date date;
    private String description;
    private String building;
    private String room;


    public Exam(ExamID id, String name, Date date, String description,
                String building, String room) {
        setId(id);

        this.setName(name);
        this.setDate(date);
        this.setDescription(description);
        this.setBuilding(building);
        this.setRoom(room);
    }

    public Exam(JSONObject obj) throws JSONException, NullPointerException, ParseException {
        // Creating mock ExamID
        setId(new ExamID(new JSONObject()));

        if (obj == null) {
            setId(null);
            throw new NullPointerException();
        }

        if (obj.has(ARG_DESCRIPTION))
            setDescription(obj.getString(ARG_DESCRIPTION));
        if (obj.has(ARG_ROOM))
            setRoom(obj.getString(ARG_ROOM));
        if (obj.has(ARG_NAME))
            setName(obj.getString(ARG_NAME));
        if (obj.has(ARG_BUILDING))
            setBuilding(obj.getString(ARG_BUILDING));
        if (obj.has(ARG_DATE))
            setDate(Utils.sdf.parse(obj.getString(ARG_DATE)));
    }


    public ExamID getId() {
        return id;
    }

    private void setId(ExamID id) {
        this.id = id;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getAddress(){
        if (!getRoom().equals("")) {
            String ris = "Milano, Bicocca ";

            try {
                ris = ris + getRoom().substring(0, getRoom().indexOf("-"));
            } catch (Exception e) {
                ris = ris + getBuilding() + " " + getRoom();
            }

            return ris;
        }
        else
            return "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }



    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ExamID.EXAM_ID, getId().toJSON());

            obj.put(ARG_NAME, getName());
            obj.put(ARG_BUILDING, getBuilding());
            obj.put(ARG_DATE, Utils.sdf.format(getDate()));
            obj.put(ARG_ROOM, getRoom());
            obj.put(ARG_DESCRIPTION, getDescription());
        } catch (JSONException e){
            obj = new JSONObject();
        }

        return obj;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Exam){
            Exam exam = (Exam) o;

            return exam.getName().equals(getName()) &&
                    exam.getDescription().equals(getDescription()) &&
                    exam.getDate().compareTo(getDate()) == 0;
        }
        return false;
    }

}
