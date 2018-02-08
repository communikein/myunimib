package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import java.util.Date;

@Entity(tableName = "available_exams", primaryKeys = {"adsceId", "appId", "attDidEsaId", "cdsEsaId"})
public class AvailableExam extends Exam {

    private Date beginEnrollment;
    private Date endEnrollment;

    public AvailableExam(int cdsEsaId, int attDidEsaId, int appId, int adsceId, String name,
                         Date date, String description, Date beginEnrollment, Date endEnrollment) {
        super(cdsEsaId, attDidEsaId, appId, adsceId, name, date, description);

        setBeginEnrollment(beginEnrollment);
        setEndEnrollment(endEnrollment);
    }

    @Ignore
    public AvailableExam(ExamID examID, String name, Date date,
                         String description, Date begin_enrollment, Date end_enrollment) {
        super(examID, name, date, description);

        setBeginEnrollment(begin_enrollment);
        setEndEnrollment(end_enrollment);
    }


    public Date getBeginEnrollment() {
        return beginEnrollment;
    }

    @Ignore
    public long getBeginMillis() {
        if (getBeginEnrollment() == null) return -1;
        else return getBeginEnrollment().getTime();
    }

    @Ignore
    public long getEndMillis() {
        if (getEndEnrollment() == null) return -1;
        else return getEndEnrollment().getTime();
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
    private void setBeginEnrollment(long millis) {
        if (millis < 0) setBeginEnrollment(null);
        else setBeginEnrollment(new Date(millis));
    }

    @Ignore
    private void setEndEnrollment(long millis) {
        if (millis < 0) setEndEnrollment(null);
        else setEndEnrollment(new Date(millis));
    }



    @Override
    public boolean isIdentic(Object obj) {
        if (! (obj instanceof AvailableExam)) return false;

        AvailableExam exam = (AvailableExam) obj;
        return super.isIdentic(exam) &&
                exam.getBeginMillis() == getBeginMillis() &&
                exam.getEndMillis() == getEndMillis();
    }

}
