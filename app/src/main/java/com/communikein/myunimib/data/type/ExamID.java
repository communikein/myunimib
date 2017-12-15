package com.communikein.myunimib.data.type;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eliam on 12/5/2017.
 */

public class ExamID {

    final public static String EXAM_ID = "EXAM_ID";

    /* The first four fields are enough to download the enrollment certificate */
    final public static String ARG_CDS_ESA_ID = "ARG_CDS_ESA_ID";
    final public static String ARG_ATT_DID_ESA_ID = "ARG_ATT_DID_ESA_ID";
    final public static String ARG_APP_ID = "ARG_APP_ID";
    final public static String ARG_ADSCE_ID = "ARG_ADSCE_ID";
    /***************************************************************************/
    final public static String ARG_DB_ID = "ARG_DB_ID";

    private int db_id = -1;
    private int cds_esa_id;
    private int att_did_esa_id;
    private int app_id;
    private int adsce_id;


    public ExamID(JSONObject obj) throws JSONException, NullPointerException {
        if (obj == null) throw new NullPointerException();

        if (obj.has(ARG_DB_ID))
            setDB_ID(obj.getInt(ARG_DB_ID));
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

    public ExamID(ExamID from) throws NullPointerException {
        if (from == null) throw new NullPointerException();

        this.db_id = from.getDB_ID();
        this.adsce_id = from.getADSCE_ID();
        this.app_id = from.getAPP_ID();
        this.att_did_esa_id = from.getATT_DID_ESA_ID();
        this.cds_esa_id = from.getCDS_ESA_ID();
    }


    public int getDB_ID() {
        return db_id;
    }

    public void setDB_ID(int DB_ID) {
        this.db_id = DB_ID;
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


    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ARG_DB_ID, getDB_ID());
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
