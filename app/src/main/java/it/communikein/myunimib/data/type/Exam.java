package it.communikein.myunimib.data.type;

import android.content.Context;

import it.communikein.myunimib.utilities.MyunimibDateUtils;
import it.communikein.myunimib.utilities.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;


public class Exam {

    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_DATE = "ARG_DATE";
    private static final String ARG_DESCRIPTION = "ARG_DESCRIPTION";

    private ExamID id;
    private String name;
    private Date date;
    private String description;


    Exam(ExamID id, String name, Date date, String description) {
        setId(id);

        this.setName(name);
        this.setDate(date);
        this.setDescription(description);
    }

    Exam(JSONObject obj) throws JSONException, NullPointerException, ParseException {
        // Creating mock ExamID
        setId(new ExamID(new JSONObject()));

        if (obj == null) {
            setId(null);
            throw new NullPointerException();
        }

        if (obj.has(ARG_DESCRIPTION))
            setDescription(obj.getString(ARG_DESCRIPTION));
        if (obj.has(ARG_NAME))
            setName(obj.getString(ARG_NAME));
        if (obj.has(ARG_DATE))
            setDate(Utils.sdf.parse(obj.getString(ARG_DATE)));
    }


    public ExamID getId() {
        return id;
    }

    private void setId(ExamID id) {
        this.id = id;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setDate(Date date) {
        this.date = date;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public String printDateTime(Context context) {
        return MyunimibDateUtils.getFriendlyDateString(context, getDate().getTime(), true);
    }

    public String getDescription() {
        return description;
    }



    JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ExamID.EXAM_ID, getId().toJSON());

            obj.put(ARG_NAME, getName());
            obj.put(ARG_DATE, Utils.sdf.format(getDate()));
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