package it.communikein.myunimib.data.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;

import it.communikein.myunimib.utilities.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

@Entity(tableName = "available_exams", indices = {@Index(value = {"adsceId"}, unique = true)})
public class AvailableExam extends Exam {

    private static final String ARG_BEGIN_ENROLLMENT = "ARG_BEGIN_ENROLLMENT";
    private static final String ARG_END_ENROLLMENT = "ARG_END_ENROLLMENT";

    private Date beginEnrollment;
    private Date endEnrollment;

    public AvailableExam(int cdsEsaId, int attDidEsaId, int appId, int adsceId, String name,
                         Date date, String description, Date beginEnrollment, Date endEnrollment) {
        super(cdsEsaId, attDidEsaId, appId, adsceId, name, date, description);

        this.beginEnrollment = beginEnrollment;
        this.endEnrollment = endEnrollment;
    }

    @Ignore
    public AvailableExam(ExamID examID, String name, Date date,
                         String description, Date begin_enrollment, Date end_enrollment) {
        super(examID, name, date, description);

        this.setBeginEnrollment(begin_enrollment);
        this.setEndEnrollment(end_enrollment);
    }

    @Ignore
    public AvailableExam(JSONObject obj) throws JSONException, NullPointerException, ParseException {
        super(obj);

        if (obj.has(ARG_BEGIN_ENROLLMENT))
            setBeginEnrollment(Utils.sdf.parse(obj.getString(ARG_BEGIN_ENROLLMENT)));
        if (obj.has(ARG_END_ENROLLMENT))
            setEndEnrollment(Utils.sdf.parse(obj.getString(ARG_END_ENROLLMENT)));
    }


    public Date getBeginEnrollment() {
        return beginEnrollment;
    }

    public Date getEndEnrollment() {
        return endEnrollment;
    }

    private void setBeginEnrollment(Date beginEnrollment) {
        this.beginEnrollment = beginEnrollment;
    }

    private void setEndEnrollment(Date endEnrollment) {
        this.endEnrollment = endEnrollment;
    }


    @Ignore
    public JSONObject toJSON() {
        JSONObject obj;

        try {
            obj = super.toJSON();

            obj.put(ARG_BEGIN_ENROLLMENT, Utils.sdf.format(getBeginEnrollment()));
            obj.put(ARG_END_ENROLLMENT, Utils.sdf.format(getEndEnrollment()));
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

}
