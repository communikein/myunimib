package com.communikein.myunimib.data.type;

import com.communikein.myunimib.utilities.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;


public class AvailableExam extends Exam {

    private static final String ARG_BEGIN_ENROLLMENT = "ARG_BEGIN_ENROLLMENT";
    private static final String ARG_END_ENROLLMENT = "ARG_END_ENROLLMENT";

    private Date begin_enrollment;
    private Date end_enrollment;

    public AvailableExam(ExamID examID, String name, Date date,
                         String description, Date begin_enrollment, Date end_enrollment) {
        super(examID, name, date, description);

        this.setBegin_enrollment(begin_enrollment);
        this.setEnd_enrollment(end_enrollment);
    }

    public AvailableExam(JSONObject obj) throws JSONException, NullPointerException, ParseException {
        super(obj);

        if (obj == null) throw new NullPointerException();

        if (obj.has(ARG_BEGIN_ENROLLMENT))
            setBegin_enrollment(Utils.sdf.parse(obj.getString(ARG_BEGIN_ENROLLMENT)));
        if (obj.has(ARG_END_ENROLLMENT))
            setEnd_enrollment(Utils.sdf.parse(obj.getString(ARG_END_ENROLLMENT)));
    }


    public Date getBegin_enrollment() {
        return begin_enrollment;
    }

    public Date getEnd_enrollment() {
        return end_enrollment;
    }

    private void setBegin_enrollment(Date begin_enrollment) {
        this.begin_enrollment = begin_enrollment;
    }

    private void setEnd_enrollment(Date end_enrollment) {
        this.end_enrollment = end_enrollment;
    }


    public JSONObject toJSON() {
        JSONObject obj;

        try {
            obj = super.toJSON();

            obj.put(ARG_BEGIN_ENROLLMENT, Utils.sdf.format(getBegin_enrollment()));
            obj.put(ARG_END_ENROLLMENT, Utils.sdf.format(getEnd_enrollment()));
        } catch (JSONException e){
            obj = new JSONObject();
        }

        return obj;
    }


    @Override
    public String toString() {
        return toJSON().toString();
    }

}
