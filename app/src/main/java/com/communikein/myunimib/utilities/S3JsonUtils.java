package com.communikein.myunimib.utilities;

import android.content.ContentValues;

import com.communikein.myunimib.data.ExamContract;
import com.communikein.myunimib.data.type.AvailableExam;
import com.communikein.myunimib.data.type.BookletEntry;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;

/**
 * Created by eliam on 12/6/2017.
 */

public class S3JsonUtils {

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     * <p/>
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param jsonString JSON response from server
     *
     * @return Array of Strings describing weather data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getBookletContentValuesFromJson(String jsonString)
            throws JSONException {

        JSONArray bookletJson = new JSONArray(jsonString);
        ContentValues[] contentValues = new ContentValues[bookletJson.length()];

        try {
            for (int i = 0; i < bookletJson.length(); i++) {
                BookletEntry entry = new BookletEntry(bookletJson.getJSONObject(i));

                ContentValues value = new ContentValues();
                value.put(
                        ExamContract.BookletEntry.COLUMN_COURSE_NAME,
                        entry.getName());
                value.put(
                        ExamContract.BookletEntry.COLUMN_DATE,
                        entry.getMillis());
                value.put(
                        ExamContract.BookletEntry.COLUMN_ADSCE_ID,
                        entry.getADSCE_ID());
                value.put(
                        ExamContract.BookletEntry.COLUMN_CFU,
                        entry.getCfu());
                value.put(
                        ExamContract.BookletEntry.COLUMN_CODE,
                        entry.getCode());
                value.put(
                        ExamContract.BookletEntry.COLUMN_MARK,
                        entry.getScore());
                value.put(
                        ExamContract.BookletEntry.COLUMN_STATE,
                        entry.getState());

                contentValues[i] = value;
            }
        } catch (ParseException e) {
            contentValues = new ContentValues[0];
        }

        return contentValues;
    }

    public static ContentValues[] getAvailableExamsValuesFromJson(String jsonString)
            throws JSONException{

        JSONArray array = new JSONArray(jsonString);
        ContentValues[] contentValues = new ContentValues[array.length()];

        try {
            for (int i = 0; i < array.length(); i++) {
                AvailableExam entry = new AvailableExam(array.getJSONObject(i));

                ContentValues value = new ContentValues();
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_COURSE_NAME,
                        entry.getName());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_DATE,
                        entry.getDate().getTime());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_DESCRIPTION,
                        entry.getDescription());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_BUILDING,
                        entry.getBuilding());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_ROOM,
                        entry.getRoom());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_BEGIN_ENROLLMENT,
                        entry.getBegin_enrollment().getTime());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_END_ENROLLMENT,
                        entry.getEnd_enrollment().getTime());

                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_ADSCE_ID,
                        entry.getId().getADSCE_ID());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_DB_ID,
                        entry.getId().getDB_ID());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_CDS_ESA_ID,
                        entry.getId().getCDS_ESA_ID());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_ATT_DID_ESA_ID,
                        entry.getId().getATT_DID_ESA_ID());
                value.put(
                        ExamContract.AvailableExamEntry.COLUMN_APP_ID,
                        entry.getId().getAPP_ID());

                contentValues[i] = value;
            }
        } catch (ParseException e) {
            contentValues = new ContentValues[0];
        }

        return contentValues;
    }
}
