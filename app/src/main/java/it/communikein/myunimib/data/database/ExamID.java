package it.communikein.myunimib.data.database;

import android.arch.persistence.room.Ignore;

import it.communikein.myunimib.data.network.UnimibNetworkDataSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ExamID {

    @Ignore
    final static String EXAM_ID = "EXAM_ID";

    private final static String ARG_CDS_ESA_ID = "ARG_CDS_ESA_ID";
    private final static String ARG_ATT_DID_ESA_ID = "ARG_ATT_DID_ESA_ID";
    private final static String ARG_APP_ID = "ARG_APP_ID";
    private final static String ARG_ADSCE_ID = "ARG_ADSCE_ID";

    private int cdsEsaId;
    private int attDidEsaId;
    private int appId;
    private int adsceId;


    @Ignore
    ExamID(JSONObject obj) throws JSONException, NullPointerException {
        if (obj == null) throw new NullPointerException();
        if (obj.has(EXAM_ID)) obj = obj.getJSONObject(EXAM_ID);

        if (obj.has(ARG_ATT_DID_ESA_ID))
            setAttDidEsaId(obj.getInt(ARG_ATT_DID_ESA_ID));
        if (obj.has(ARG_CDS_ESA_ID))
            setCdsEsaId(obj.getInt(ARG_CDS_ESA_ID));
        if (obj.has(ARG_APP_ID))
            setAppId(obj.getInt(ARG_APP_ID));
        if (obj.has(ARG_ADSCE_ID))
            setAdsceId(obj.getInt(ARG_ADSCE_ID));
    }

    public ExamID(int cdsEsaId, int attDidEsaId, int appId, int adsceId) {
        this.cdsEsaId = cdsEsaId;
        this.attDidEsaId = attDidEsaId;
        this.appId = appId;
        this.adsceId = adsceId;
    }

    @Ignore
    public ExamID(String app_id, String cds_esa_id, String att_did_esa_id, String adsce_id) {
        this.adsceId = Integer.parseInt(adsce_id);
        this.appId = Integer.parseInt(app_id);
        this.attDidEsaId = Integer.parseInt(att_did_esa_id);
        this.cdsEsaId = Integer.parseInt(cds_esa_id);
    }


    public int getCdsEsaId() {
        return cdsEsaId;
    }

    private void setCdsEsaId(int cdsEsaId) {
        this.cdsEsaId = cdsEsaId;
    }

    public int getAttDidEsaId() {
        return attDidEsaId;
    }

    private void setAttDidEsaId(int attDidEsaId) {
        this.attDidEsaId = attDidEsaId;
    }

    public int getAppId() {
        return appId;
    }

    private void setAppId(int appId) {
        this.appId = appId;
    }

    public int getAdsceId() {
        return adsceId;
    }

    private void setAdsceId(int adsceId) {
        this.adsceId = adsceId;
    }

    @Ignore
    public String examIdToUrl() {
        try {
            String adsce_id = URLEncoder.encode(String.valueOf(getAdsceId()), "UTF-8");
            String att_did_esa_id = URLEncoder.encode(String.valueOf(getAttDidEsaId()), "UTF-8");
            String app_id = URLEncoder.encode(String.valueOf(getAppId()), "UTF-8");
            String cds_esa_id = URLEncoder.encode(String.valueOf(getCdsEsaId()), "UTF-8");

            return UnimibNetworkDataSource.ADSCE_ID + "=" + adsce_id + "&" +
                    UnimibNetworkDataSource.APP_ID + "=" + app_id + "&" +
                    UnimibNetworkDataSource.ATT_DID_ESA_ID + "=" + att_did_esa_id + "&" +
                    UnimibNetworkDataSource.CDS_ESA_ID + "=" + cds_esa_id;
        } catch (UnsupportedEncodingException e){
            return null;
        }
    }


    @Ignore
    JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ARG_CDS_ESA_ID, getCdsEsaId());
            obj.put(ARG_APP_ID, getAppId());
            obj.put(ARG_ADSCE_ID, getAdsceId());
            obj.put(ARG_ATT_DID_ESA_ID, getAttDidEsaId());
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

    @Ignore
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof  ExamID)) return false;

        ExamID examID = (ExamID) obj;
        return examID.getAdsceId() == this.getAdsceId();
    }

    public boolean isIdentic(Object obj) {
        if (! (obj instanceof ExamID)) return false;

        ExamID examID = (ExamID) obj;
        return equals(obj) &&
                examID.getAppId() == this.getAppId() &&
                examID.getAttDidEsaId() == this.getAttDidEsaId() &&
                examID.getCdsEsaId() == this.getCdsEsaId();
    }
}
