package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Ignore;
import android.content.Context;
import android.os.Environment;

import it.communikein.myunimib.utilities.DateHelper;

import java.io.File;
import java.util.Date;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Exam extends ExamID {

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

    public long getDateMillis() {
        if (getDate() == null) return -1;
        else return getDate().getTime();
    }

    @Ignore
    public String printDateTime(Context context) {
        return DateHelper.getFriendlyDateString(context, getDate().getTime(), true, true);
    }

    public String getDescription() {
        return description;
    }




    @Override
    boolean isIdentic(Object obj) {
        if (! (obj instanceof Exam)) return false;

        Exam exam = (Exam) obj;
        return super.isIdentic(exam) &&
                exam.getName().equals(getName()) &&
                exam.getDescription().equals(getDescription()) &&
                exam.getDateMillis() == getDateMillis();
    }


    public String printFriendlyDate(Context context) {
        return DateHelper.getFriendlyDateString(
                context,
                getDate().getTime(),
                false,
                false);
    }

    @Ignore
    private String getCertificateName(){
        return getName() + " - " + DateHelper.dateFile.format(getDate()) + ".pdf";
    }

    @Ignore
    public File getCertificatePath() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                getCertificateName());
    }
}
