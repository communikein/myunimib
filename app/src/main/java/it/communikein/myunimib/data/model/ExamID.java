package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Ignore;

import it.communikein.myunimib.data.network.UnimibNetworkDataSource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ExamID {

    @Ignore
    final static String EXAM_ID = "EXAM_ID";

    private int cdsEsaId;
    private int attDidEsaId;
    private int appId;
    private int adsceId;


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
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof  ExamID)) return false;

        ExamID examID = (ExamID) obj;
        return examID.getAdsceId() == this.getAdsceId();
    }

    boolean isIdentic(Object obj) {
        if (! (obj instanceof ExamID)) return false;

        ExamID examID = (ExamID) obj;
        return equals(obj) &&
                examID.getAppId() == this.getAppId() &&
                examID.getAttDidEsaId() == this.getAttDidEsaId() &&
                examID.getCdsEsaId() == this.getCdsEsaId();
    }
}
