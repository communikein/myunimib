package it.communikein.myunimib.data.type;

import it.communikein.myunimib.sync.SyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class ExamID {

    final public static String EXAM_ID = "EXAM_ID";

    private final static String ARG_CDS_ESA_ID = "ARG_CDS_ESA_ID";
    private final static String ARG_ATT_DID_ESA_ID = "ARG_ATT_DID_ESA_ID";
    private final static String ARG_APP_ID = "ARG_APP_ID";
    private final static String ARG_ADSCE_ID = "ARG_ADSCE_ID";

    private int cds_esa_id;
    private int att_did_esa_id;
    private int app_id;
    private int adsce_id;


    public ExamID(JSONObject obj) throws JSONException, NullPointerException {
        if (obj == null) throw new NullPointerException();

        if (obj.has(ARG_ATT_DID_ESA_ID))
            setATT_DID_ESA_ID(obj.getInt(ARG_ATT_DID_ESA_ID));
        if (obj.has(ARG_CDS_ESA_ID))
            setCDS_ESA_ID(obj.getInt(ARG_CDS_ESA_ID));
        if (obj.has(ARG_APP_ID))
            setAPP_ID(obj.getInt(ARG_APP_ID));
        if (obj.has(ARG_ADSCE_ID))
            setADSCE_ID(obj.getInt(ARG_ADSCE_ID));
    }

    public ExamID(int app_id, int cds_esa_id, int att_did_esa_id, int adsce_id) {
        this.adsce_id = adsce_id;
        this.app_id = app_id;
        this.att_did_esa_id = att_did_esa_id;
        this.cds_esa_id = cds_esa_id;
    }

    public ExamID(String app_id, String cds_esa_id, String att_did_esa_id, String adsce_id) {
        this.adsce_id = Integer.parseInt(adsce_id);
        this.app_id = Integer.parseInt(app_id);
        this.att_did_esa_id = Integer.parseInt(att_did_esa_id);
        this.cds_esa_id = Integer.parseInt(cds_esa_id);
    }


    public int getADSCE_ID() {
        return adsce_id;
    }

    private void setADSCE_ID(int adsce_id) {
        this.adsce_id = adsce_id;
    }

    public int getAPP_ID() {
        return app_id;
    }

    private void setAPP_ID(int app_id) {
        this.app_id = app_id;
    }

    public int getATT_DID_ESA_ID() {
        return att_did_esa_id;
    }

    private void setATT_DID_ESA_ID(int att_did_esa_id) {
        this.att_did_esa_id = att_did_esa_id;
    }

    public int getCDS_ESA_ID() {
        return cds_esa_id;
    }

    private void setCDS_ESA_ID(int cds_esa_id) {
        this.cds_esa_id = cds_esa_id;
    }


    public String examIdToUrl() {
        try {
            String adsce_id = URLEncoder.encode(String.valueOf(getADSCE_ID()), "UTF-8");
            String att_did_esa_id = URLEncoder.encode(String.valueOf(getATT_DID_ESA_ID()), "UTF-8");
            String app_id = URLEncoder.encode(String.valueOf(getAPP_ID()), "UTF-8");
            String cds_esa_id = URLEncoder.encode(String.valueOf(getCDS_ESA_ID()), "UTF-8");

            return SyncTask.ADSCE_ID + "=" + adsce_id + "&" +
                    SyncTask.APP_ID + "=" + app_id + "&" +
                    SyncTask.ATT_DID_ESA_ID + "=" + att_did_esa_id + "&" +
                    SyncTask.CDS_ESA_ID + "=" + cds_esa_id;
        } catch (UnsupportedEncodingException e){
            return null;
        }
    }



    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ARG_CDS_ESA_ID, getCDS_ESA_ID());
            obj.put(ARG_APP_ID, getAPP_ID());
            obj.put(ARG_ADSCE_ID, getADSCE_ID());
            obj.put(ARG_ATT_DID_ESA_ID, getATT_DID_ESA_ID());
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