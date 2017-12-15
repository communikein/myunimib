package com.communikein.myunimib.data.type;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by eliam on 12/7/2017.
 */

public class ExamEnrollmentInfo {

    public static final String ENROLLMENT_INFO = "ENROLLMENT_INFO";
    public static final String ENROLLMENT_INFO_KEYS = "ENROLLMENT_INFO_KEYS";
    public static final String ENROLLMENT_INFO_VALUES = "ENROLLMENT_INFO_VALUES";

    private static final String ARG_AA_OFF_ID = "ARG_AA_OFF_ID";
    private static final String ARG_CDS_ID = "ARG_CDS_ID";
    private static final String ARG_PDS_ID = "ARG_PDS_ID";
    private static final String ARG_AA_ORD_ID = "ARG_AA_ORD_ID";
    private static final String ARG_ISCR_APERTA = "ARG_ISCR_APERTA";
    private static final String ARG_TIPO_ATTIVITA = "ARG_TIPO_ATTIVITA";
    private static final String ARG_TIPO_APP_COD = "ARG_TIPO_APP_COD";


    private ExamID id;
    private String mAA_OFF_ID = "";
    private String mCDS_ID = "";
    private String mPDS_ID = "";
    private String mAA_ORD_ID = "";
    private String mISCR_APERTA = "";
    private String mTIPO_ATTIVITA = "";
    private String mTIPO_APP_COD = "";


    public ExamEnrollmentInfo(ExamID id,
                              String AA_OFF_ID, String CDS_ID, String PDS_ID, String AA_ORD_ID,
                              String ISCR_APERTA, String TIPO_ATTIVITA, String TIPO_APP_COD) {
        if (id != null) this.id = id;
        if (AA_OFF_ID != null) this.mAA_OFF_ID = AA_OFF_ID;
        if (CDS_ID != null) this.mCDS_ID = CDS_ID;
        if (PDS_ID != null) this.mPDS_ID = PDS_ID;
        if (AA_ORD_ID != null) this.mAA_ORD_ID = AA_ORD_ID;
        if (ISCR_APERTA != null) this.mISCR_APERTA = ISCR_APERTA;
        if (TIPO_ATTIVITA != null) this.mTIPO_ATTIVITA = TIPO_ATTIVITA;
        if (TIPO_APP_COD != null) this.mTIPO_APP_COD = TIPO_APP_COD;
    }

    public ExamEnrollmentInfo(JSONObject obj) throws JSONException, NullPointerException {
        if (obj == null) throw new NullPointerException();

        if (obj.has(ExamID.EXAM_ID))
            setExamID(new ExamID(obj.getJSONObject(ExamID.EXAM_ID)));
        if (obj.has(ARG_AA_OFF_ID))
            setAA_OFF_ID(obj.getString(ARG_AA_OFF_ID));
        if (obj.has(ARG_CDS_ID))
            setCDS_ID(obj.getString(ARG_CDS_ID));
        if (obj.has(ARG_PDS_ID))
            setPDS_ID(obj.getString(ARG_PDS_ID));
        if (obj.has(ARG_AA_ORD_ID))
            setAA_ORD_ID(obj.getString(ARG_AA_ORD_ID));
        if (obj.has(ARG_ISCR_APERTA))
            setISCR_APERTA(obj.getString(ARG_ISCR_APERTA));
        if (obj.has(ARG_TIPO_ATTIVITA))
            setTIPO_ATTIVITA(obj.getString(ARG_TIPO_ATTIVITA));
        if (obj.has(ARG_TIPO_APP_COD))
            setTIPO_APP_COD(obj.getString(ARG_TIPO_APP_COD));
    }


    public ExamEnrollmentInfo(HashMap<String, String> values){
        id = new ExamID(Integer.parseInt(values.get("APP_ID")),
                Integer.parseInt(values.get("CDS_ESA_ID")),
                Integer.parseInt(values.get("ATT_DID_ESA_ID")),
                Integer.parseInt(values.get("ADSCE_ID")));

        for (String name : values.keySet()){
            switch(name){
                case "AA_OFF_ID":
                    mAA_OFF_ID = values.get(name);
                    break;
                case "CDS_ID":
                    mCDS_ID = values.get(name);
                    break;
                case "PDS_ID":
                    mPDS_ID = values.get(name);
                    break;
                case "AA_ORD_ID":
                    mAA_ORD_ID = values.get(name);
                    break;
                case "ISCR_APERTA":
                    mISCR_APERTA = values.get(name);
                    break;
                case "TIPO_ATTIVITA":
                    mTIPO_ATTIVITA = values.get(name);
                    break;
                case "TIPO_APP_COD":
                    mTIPO_APP_COD = values.get(name);
                    break;
            }
        }
    }

    public boolean areInfosSetted(){
        for(CharSequence info : getValues())
            if (!info.equals("")) return true;

        return false;
    }

    public ArrayList<CharSequence> getNames(){
        ArrayList<CharSequence> names = new ArrayList<>();

        names.add("APP_ID");
        names.add("CDS_ESA_ID");
        names.add("ATT_DID_ESA_ID");
        names.add("ADSCE_ID");
        names.add("AA_OFF_ID");
        names.add("CDS_ID");
        names.add("PDS_ID");
        names.add("AA_ORD_ID");
        names.add("ISCR_APERTA");
        names.add("TIPO_ATTIVITA");
        names.add("TIPO_APP_COD");

        return names;
    }

    public ArrayList<CharSequence> getValues(){
        ArrayList<CharSequence> values = new ArrayList<>();

        values.add(String.valueOf(id.getAPP_ID()));
        values.add(String.valueOf(id.getCDS_ESA_ID()));
        values.add(String.valueOf(id.getATT_DID_ESA_ID()));
        values.add(String.valueOf(id.getADSCE_ID()));
        values.add(mAA_OFF_ID);
        values.add(mCDS_ID);
        values.add(mPDS_ID);
        values.add(mAA_ORD_ID);
        values.add(mISCR_APERTA);
        values.add(mTIPO_ATTIVITA);
        values.add(mTIPO_APP_COD);

        return values;
    }

    public String getAA_OFF_ID() {
        return mAA_OFF_ID;
    }

    public String getCDS_ID() {
        return mCDS_ID;
    }

    public String getPDS_ID() {
        return mPDS_ID;
    }

    public String getAA_ORD_ID() {
        return mAA_ORD_ID;
    }

    public String getISCR_APERTA() {
        return mISCR_APERTA;
    }

    public String getTIPO_ATTIVITA() {
        return mTIPO_ATTIVITA;
    }

    public String getTIPO_APP_COD() {
        return mTIPO_APP_COD;
    }

    public ExamID getId() {
        return id;
    }

    private void setAA_OFF_ID(String AA_OFF_ID) {
        this.mAA_OFF_ID = AA_OFF_ID;
    }

    private void setCDS_ID(String CDS_ID) {
        this.mCDS_ID = CDS_ID;
    }

    private void setPDS_ID(String PDS_ID) {
        this.mPDS_ID = PDS_ID;
    }

    private void setAA_ORD_ID(String AA_ORD_ID) {
        this.mAA_ORD_ID = AA_ORD_ID;
    }

    private void setISCR_APERTA(String ISCR_APERTA) {
        this.mISCR_APERTA = ISCR_APERTA;
    }

    private void setTIPO_ATTIVITA(String TIPO_ATTIVITA) {
        this.mTIPO_ATTIVITA = TIPO_ATTIVITA;
    }

    private void setTIPO_APP_COD(String TIPO_APP_COD) {
        this.mTIPO_APP_COD = TIPO_APP_COD;
    }

    private void setExamID(ExamID examID) { this.id = examID; }


    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ARG_AA_OFF_ID, getAA_OFF_ID());
            obj.put(ARG_AA_ORD_ID, getAA_ORD_ID());
            obj.put(ARG_CDS_ID, getCDS_ID());
            obj.put(ARG_ISCR_APERTA, getISCR_APERTA());
            obj.put(ARG_PDS_ID, getPDS_ID());
            obj.put(ARG_TIPO_APP_COD, getTIPO_APP_COD());
            obj.put(ARG_TIPO_ATTIVITA, getTIPO_ATTIVITA());
            obj.put(ExamID.EXAM_ID, getId().toJSON());
        } catch (JSONException e) {
            obj = new JSONObject();
        }

        return obj;
    }


    @Override
    public String toString(){
        return toJSON().toString();
    }

}
