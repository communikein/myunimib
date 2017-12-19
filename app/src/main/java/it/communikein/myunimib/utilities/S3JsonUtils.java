package it.communikein.myunimib.utilities;

import android.content.ContentValues;

import it.communikein.myunimib.data.ExamContract;
import it.communikein.myunimib.data.type.AvailableExam;
import it.communikein.myunimib.data.type.BookletEntry;
import it.communikein.myunimib.data.type.EnrolledExam;

import java.util.ArrayList;


public class S3JsonUtils {

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     * <p/>
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param exams JSON response from server
     *
     * @return Array of Strings describing weather data
     */
    public static ContentValues[] getBookletContentValues(ArrayList<BookletEntry> exams) {
        ContentValues[] contentValues = new ContentValues[exams.size()];

        int cont = 0;
        for (BookletEntry entry : exams) {
            ContentValues value = new ContentValues();

            value.put(ExamContract.BookletEntry.COLUMN_COURSE_NAME,
                    entry.getName());
            value.put(ExamContract.BookletEntry.COLUMN_DATE,
                    entry.getMillis());
            value.put(ExamContract.BookletEntry.COLUMN_ADSCE_ID,
                    entry.getADSCE_ID());
            value.put(ExamContract.BookletEntry.COLUMN_CFU,
                    entry.getCfu());
            value.put(ExamContract.BookletEntry.COLUMN_CODE,
                    entry.getCode());
            value.put(ExamContract.BookletEntry.COLUMN_MARK,
                    entry.getScore());
            value.put(ExamContract.BookletEntry.COLUMN_STATE,
                    entry.getState());

            contentValues[cont++] = value;
        }

        return contentValues;
    }

    public static ContentValues[] getAvailableExamsContentValues(ArrayList<AvailableExam> exams) {
        ContentValues[] contentValues = new ContentValues[exams.size()];

        int cont = 0;
        for (AvailableExam entry : exams) {
            ContentValues value = new ContentValues();

            value.put(ExamContract.AvailableExamEntry.COLUMN_COURSE_NAME,
                    entry.getName());
            value.put(ExamContract.AvailableExamEntry.COLUMN_DATE,
                    entry.getDate().getTime());
            value.put(ExamContract.AvailableExamEntry.COLUMN_DESCRIPTION,
                    entry.getDescription());
            value.put(ExamContract.AvailableExamEntry.COLUMN_BEGIN_ENROLLMENT,
                    entry.getBegin_enrollment().getTime());
            value.put(ExamContract.AvailableExamEntry.COLUMN_END_ENROLLMENT,
                    entry.getEnd_enrollment().getTime());

            value.put(ExamContract.AvailableExamEntry.COLUMN_ADSCE_ID,
                    entry.getId().getADSCE_ID());
            value.put(ExamContract.AvailableExamEntry.COLUMN_CDS_ESA_ID,
                    entry.getId().getCDS_ESA_ID());
            value.put(ExamContract.AvailableExamEntry.COLUMN_ATT_DID_ESA_ID,
                    entry.getId().getATT_DID_ESA_ID());
            value.put(ExamContract.AvailableExamEntry.COLUMN_APP_ID,
                    entry.getId().getAPP_ID());

            contentValues[cont++] = value;
        }

        return contentValues;
    }

    public static ContentValues[] getEnrolledExamsContentValues(ArrayList<EnrolledExam> exams) {
        ContentValues[] contentValues = new ContentValues[exams.size()];

        int cont = 0;
        for (EnrolledExam entry : exams) {
            ContentValues value = new ContentValues();

            value.put(ExamContract.EnrolledExamEntry.COLUMN_COURSE_NAME,
                    entry.getName());
            value.put(ExamContract.EnrolledExamEntry.COLUMN_DATE,
                    entry.getDate().getTime());
            value.put(ExamContract.EnrolledExamEntry.COLUMN_CODE,
                    entry.getCode());
            value.put(ExamContract.EnrolledExamEntry.COLUMN_DESCRIPTION,
                    entry.getDescription());
            value.put(ExamContract.EnrolledExamEntry.COLUMN_BUILDING,
                    entry.getBuilding());
            value.put(ExamContract.EnrolledExamEntry.COLUMN_ROOM,
                    entry.getRoom());
            value.put(ExamContract.EnrolledExamEntry.COLUMN_RESERVED,
                    entry.getReserved());
            value.put(ExamContract.EnrolledExamEntry.COLUMN_TEACHERS,
                    entry.getTeachersJson().toString());

            value.put(ExamContract.AvailableExamEntry.COLUMN_ADSCE_ID,
                    entry.getId().getADSCE_ID());
            value.put(ExamContract.AvailableExamEntry.COLUMN_CDS_ESA_ID,
                    entry.getId().getCDS_ESA_ID());
            value.put(ExamContract.AvailableExamEntry.COLUMN_ATT_DID_ESA_ID,
                    entry.getId().getATT_DID_ESA_ID());
            value.put(ExamContract.AvailableExamEntry.COLUMN_APP_ID,
                    entry.getId().getAPP_ID());

            contentValues[cont++] = value;
        }

        return contentValues;
    }
}
