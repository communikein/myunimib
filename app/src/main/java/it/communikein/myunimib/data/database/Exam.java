package it.communikein.myunimib.data.database;

import android.arch.persistence.room.Ignore;
import android.content.Context;
import android.os.Environment;

import it.communikein.myunimib.utilities.MyunimibDateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

public class Exam extends ExamID {

    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_DATE = "ARG_DATE";
    private static final String ARG_DESCRIPTION = "ARG_DESCRIPTION";

    private String name;
    private Date date;
    private String description;


    Exam(int cdsEsaId, int attDidEsaId, int appId, int adsceId, String name, Date date,
         String description) {
        super(cdsEsaId, attDidEsaId, appId, adsceId);

        setName(name);
        setDate(date);
        setDescription(description);
    }

    @Ignore
    Exam(ExamID id, String name, Date date, String description) {
        super(id.getCdsEsaId(), id.getAttDidEsaId(), id.getAppId(), id.getAdsceId());

        setName(name);
        setDate(date);
        setDescription(description);
    }

    @Ignore
    Exam(JSONObject obj) throws JSONException, NullPointerException {
        super(obj);

        if (obj.has(ARG_DESCRIPTION))
            setDescription(obj.getString(ARG_DESCRIPTION));
        if (obj.has(ARG_NAME))
            setName(obj.getString(ARG_NAME));
        if (obj.has(ARG_DATE))
            setDate(obj.getLong(ARG_DATE));
    }


    private void setName(String name) {
        if (name == null) this.name = "";
        else this.name = name;
    }

    private void setDate(Date date) {
        this.date = date;
    }

    @Ignore
    private void setDate(long millis) {
        if (millis < 0) setDate(null);
        else setDate(new Date(millis));
    }

    private void setDescription(String description) {
        if (description == null) this.description = "";
        else this.description = description;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public long getMillis() {
        if (getDate() == null) return -1;
        else return getDate().getTime();
    }

    @Ignore
    public String printDateTime(Context context) {
        return MyunimibDateUtils.getFriendlyDateString(context, getDate().getTime(), true, true);
    }

    public String getDescription() {
        return description;
    }


    @Ignore
    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ExamID.EXAM_ID, super.toJSON());

            obj.put(ARG_NAME, getName());
            obj.put(ARG_DATE, getMillis());
            obj.put(ARG_DESCRIPTION, getDescription());
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

    @Override
    public boolean isIdentic(Object obj) {
        if (! (obj instanceof Exam)) return false;

        Exam exam = (Exam) obj;
        return super.isIdentic(exam) &&
                exam.getName().equals(getName()) &&
                exam.getDescription().equals(getDescription()) &&
                exam.getMillis() == getMillis();
    }


    @Ignore
    public String getCertificateName(){
        return getName() + " - " + MyunimibDateUtils.dateFile.format(getDate()) + ".pdf";
    }

    @Ignore
    public File getCertificatePath() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                getCertificateName());
    }
}
