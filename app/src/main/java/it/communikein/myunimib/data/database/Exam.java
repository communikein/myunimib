package it.communikein.myunimib.data.database;

import android.arch.persistence.room.Ignore;
import android.content.Context;

import it.communikein.myunimib.utilities.MyunimibDateUtils;
import it.communikein.myunimib.utilities.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
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

        this.name = name;
        this.date = date;
        this.description = description;
    }

    @Ignore
    Exam(ExamID id, String name, Date date, String description) {
        super(id.getAppId(), id.getCdsEsaId(), id.getAttDidEsaId(), id.getAdsceId());

        this.setName(name);
        this.setDate(date);
        this.setDescription(description);
    }

    @Ignore
    Exam(JSONObject obj) throws JSONException, NullPointerException, ParseException {
        super(obj);

        if (obj.has(ARG_DESCRIPTION))
            setDescription(obj.getString(ARG_DESCRIPTION));
        if (obj.has(ARG_NAME))
            setName(obj.getString(ARG_NAME));
        if (obj.has(ARG_DATE))
            setDate(Utils.sdf.parse(obj.getString(ARG_DATE)));
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

    @Ignore
    public String printDateTime(Context context) {
        return MyunimibDateUtils.getFriendlyDateString(context, getDate().getTime(), true);
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
            obj.put(ARG_DATE, Utils.sdf.format(getDate()));
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
}
