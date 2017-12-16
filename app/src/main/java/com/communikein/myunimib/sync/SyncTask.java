package com.communikein.myunimib.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.communikein.myunimib.User;
import com.communikein.myunimib.data.ExamContract;
import com.communikein.myunimib.data.type.AvailableExam;
import com.communikein.myunimib.data.type.BookletEntry;
import com.communikein.myunimib.data.type.EnrolledExam;
import com.communikein.myunimib.data.type.ExamID;
import com.communikein.myunimib.sync.availableexams.SyncUtilsAvailable;
import com.communikein.myunimib.sync.booklet.SyncUtilsBooklet;
import com.communikein.myunimib.sync.enrolledexams.SyncUtilsEnrolled;
import com.communikein.myunimib.utilities.MyunimibDateUtils;
import com.communikein.myunimib.utilities.S3JsonUtils;
import com.communikein.myunimib.utilities.UserUtils;
import com.communikein.myunimib.utilities.Utils;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import static com.communikein.myunimib.sync.S3Helper.getHTML;

public class SyncTask {

    final private static String CDS_ESA_ID = "CDS_ESA_ID";
    final private static String ATT_DID_ESA_ID = "ATT_DID_ESA_ID";
    final private static String APP_ID = "APP_ID";
    final private static String ADSCE_ID = "ADSCE_ID";

    private static final String PARAM_KEY_HTML = "param-key-html";
    private static final String PARAM_KEY_RESPONSE = "param-key-response";

    /**
     * Performs the network request for updated booklet info, parses the JSON from that request,
     * and inserts the new information into the ContentProvider. Will notify the user if new
     * data has been loaded (provided the user they haven't disabled notifications in the
     * preferences screen).
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncBooklet(Context context) {

        try {
            /* Retrieve the JSON */
            String jsonResponse = downloadBooklet(context).toString();

            /* Parse the JSON into a list of booklet values */
            ContentValues[] bookletValues = S3JsonUtils
                    .getBookletContentValuesFromJson(jsonResponse);

            if (bookletValues != null && bookletValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver myunimibContentResolver = context.getContentResolver();

                /* Delete old booklet data */
                myunimibContentResolver.delete(
                        ExamContract.BookletEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert new booklet data into the ContentProvider */
                myunimibContentResolver.bulkInsert(
                        ExamContract.BookletEntry.CONTENT_URI,
                        bookletValues);
            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }

    /**
     * Performs the network request for updated available exams info, parses the JSON from that
     * request, and inserts the new information into the ContentProvider. Will notify the user if
     * new data has been loaded (provided the user they haven't disabled notifications in the
     * preferences screen).
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncAvailableExams(Context context) {

        try {
            /* Retrieve the JSON */
            String jsonResponse = downloadAvailableExams(context).toString();

            /* Parse the JSON into a list of exams values */
            ContentValues[] examsValues = S3JsonUtils
                    .getAvailableExamsValuesFromJson(jsonResponse);

            if (examsValues != null && examsValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver myunimibContentResolver = context.getContentResolver();

                /* Delete old booklet data */
                myunimibContentResolver.delete(
                        ExamContract.AvailableExamEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert new booklet data into the ContentProvider */
                myunimibContentResolver.bulkInsert(
                        ExamContract.AvailableExamEntry.CONTENT_URI,
                        examsValues);
            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }

    /**
     * Performs the network request for updated available exams info, parses the JSON from that
     * request, and inserts the new information into the ContentProvider. Will notify the user if
     * new data has been loaded (provided the user they haven't disabled notifications in the
     * preferences screen).
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncEnrolledExams(Context context) {

        try {
            /* Retrieve the JSON */
            String jsonResponse = downloadEnrolledExams(context).toString();

            /* Parse the JSON into a list of exams values */
            ContentValues[] examsValues = S3JsonUtils
                    .getEnrolledExamsValuesFromJson(jsonResponse);

            if (examsValues != null && examsValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver myunimibContentResolver = context.getContentResolver();

                /* Delete old booklet data */
                myunimibContentResolver.delete(
                        ExamContract.EnrolledExamEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert new booklet data into the ContentProvider */
                myunimibContentResolver.bulkInsert(
                        ExamContract.EnrolledExamEntry.CONTENT_URI,
                        examsValues);
            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }



    private static JSONArray downloadBooklet(Context context) {
        if (context == null)
            return null;

        User user = UserUtils.getUser(context);

        try {
            // Try to get the private page
            Bundle result = tryGetUrlWithLogin(S3Helper.URL_LIBRETTO, user, context);

            String html = result.getString(PARAM_KEY_HTML);
            int s3_response = result.getInt(PARAM_KEY_RESPONSE);

            if (html != null && s3_response == HttpURLConnection.HTTP_OK) {
                Document doc = Jsoup.parse(html);
                Elements els = doc.select("div#esse3old table.detail_table tr");
                // Rimuovi la riga dell'intestazione
                els.remove(0);

                ArrayList<BookletEntry> booklet = new ArrayList<>();
                for (int i = 0; i < els.size(); i++) {
                    Element el = els.get(i);

                    int index;
                    if (el.child(1).children().size() > 0)
                        index = 0;
                    else
                        index = 1;

                    String exam_name = el.child(1 - index).child(index).text();
                    String adsce_id_txt = el.child(1 - index).child(index).attr("href");
                    adsce_id_txt = adsce_id_txt.substring(
                            adsce_id_txt.indexOf("?adsce_id=") + 10,
                            adsce_id_txt.indexOf("&"));
                    int adsce_id = Integer.parseInt(adsce_id_txt);
                    String code;
                    // TODO: can CFU be floats and not integer???! O.o
                    int cfu = 0;
                    if (!el.child(6 - index).text().equals(""))
                        // TODO: found a user with value '5.5'
                        cfu = Integer.parseInt(el.child(6 - index).text());
                    String state = el.child(7 - index).child(0).attr("src");
                    String mark = el.child(9 - index).text();
                    String date = el.child(9 - index).text();
                    Date dateStart = null;

                    code = exam_name.substring(0, exam_name.indexOf(" - "));
                    exam_name = exam_name.substring(exam_name.indexOf(" - ") + 3);
                    if (!date.equals("")) {
                        date = date.substring(date.indexOf(" - ") + 3);
                        dateStart = MyunimibDateUtils.dateFormat.parse(date);
                    }
                    if (!mark.equals("")) {
                        mark = mark.toLowerCase();
                        mark = mark.substring(0, mark.indexOf(" - "));
                    }
                    if (!state.equals(""))
                        state = state.substring(
                                state.lastIndexOf("/") + 1,
                                state.indexOf("."));

                    BookletEntry newExam = new BookletEntry(adsce_id, exam_name, dateStart,
                            cfu, state, mark, code);
                    booklet.add(newExam);
                }

                JSONArray array = new JSONArray();
                if (booklet.size() > 0)
                    array = UserUtils.bookletToJson(booklet);

                if (array != null)
                    return array;
                else
                    return new JSONArray();
            }
        } catch (SocketTimeoutException e) {
            Log.i(SyncUtilsBooklet.REMINDER_JOB_TAG, "SOCKET_TIMEOUT");
        } catch (Exception e) {
            Log.e(SyncUtilsBooklet.REMINDER_JOB_TAG, e.getMessage());
            Utils.saveBugReport(e);
        }

        return null;
    }

    private static JSONArray downloadAvailableExams(Context context) {
        if (context == null)
            return null;

        User user = UserUtils.getUser(context);

        try {
            // Try to get the private page
            Bundle result = tryGetUrlWithLogin(S3Helper.URL_AVAILABLE_EXAMS, user, context);

            String html = result.getString(PARAM_KEY_HTML);
            int s3_response = result.getInt(PARAM_KEY_RESPONSE);

            // Se l'utente è autenticato
            if (html != null && s3_response == S3Helper.OK_LOGGED_IN) {
                Document doc = Jsoup.parse(html);
                Elements rows = doc.select("table#app-tabella_appelli tbody tr");

                // If there's at least one exam available
                ArrayList<AvailableExam> exams = new ArrayList<>();
                for (Element el : rows) {
                    String extraInfoString = el
                            .child(0)
                            .select("a#app-toolbarTipoAppello")
                            .first()
                            .attr("href");
                    ExamID examID = getExamIdFromUrl(extraInfoString);

                    String name = el.child(1).text();
                    String date_str = el.child(2).text();
                    String[] enrollment_window_str = el.child(3).text().split(" ");
                    String description = el.child(4).text();

                    Date date = MyunimibDateUtils.dateFormat.parse(date_str);
                    Date enrollment_window_begin = MyunimibDateUtils.dateFormat
                            .parse(enrollment_window_str[0]);
                    Date enrollment_window_end = MyunimibDateUtils.dateFormat
                            .parse(enrollment_window_str[1]);

                    AvailableExam exam = new AvailableExam(
                            examID,
                            name,
                            date,
                            description,
                            enrollment_window_begin,
                            enrollment_window_end);

                    exams.add(exam);
                }

                JSONArray array = new JSONArray();
                if (exams.size() > 0)
                    array = UserUtils.availableExamsToJson(exams);

                if (array != null)
                    return array;
                else
                    return new JSONArray();
            }
        } catch (SocketTimeoutException e){
            Log.i(SyncUtilsAvailable.REMINDER_JOB_TAG, "SOCKET_TIMEOUT");
        } catch (Exception e) {
            Log.e(SyncUtilsAvailable.REMINDER_JOB_TAG, e.getMessage());
            Utils.saveBugReport(e);
        }

        return null;
    }

    private static JSONArray downloadEnrolledExams(Context context) {
        if (context == null)
            return null;

        User user = UserUtils.getUser(context);

        try {
            // Try to get the private page
            Bundle result = tryGetUrlWithLogin(S3Helper.URL_ENROLLED_EXAMS, user, context);

            String html = result.getString(PARAM_KEY_HTML);
            int s3_response = result.getInt(PARAM_KEY_RESPONSE);

            // Se l'utente è autenticato
            if (html != null && s3_response == HttpURLConnection.HTTP_OK) {
                Document doc = Jsoup.parse(html);
                Elements rows = doc.select("div#esse3old table.detail_table");

                ArrayList<EnrolledExam> exams = new ArrayList<>();
                for (Element el : rows) {
                    Elements exam_rows = el.select("tr");
                    String exam_name = exam_rows.get(0).text();
                    String code = exam_name;
                    String description = exam_name;
                    exam_name = exam_name.substring(0, exam_name.indexOf(" - ["));
                    code = code.substring(code.indexOf(" - [") + 4, code.indexOf("] - "));
                    description = description.substring(description.indexOf("] - ") + 4);

                    Element exam_data = exam_rows.get(5);
                    String date = exam_data.child(0).text();
                    String time = exam_data.child(1).text();
                    String building = exam_data.child(2).text();
                    String room = exam_data.child(3).text();
                    String reserved = exam_data.child(4).text();
                    ArrayList<String> teachers = getTeachers(exam_rows);
                    String dateTimeTmp = date;
                    if (!time.isEmpty()) dateTimeTmp += " " + time;
                    dateTimeTmp = dateTimeTmp.replaceAll("\\s+", " ");
                    Date dateStart = MyunimibDateUtils.dateTimeFormat.parse(dateTimeTmp);

                    // Save the exams ID
                    ExamID examID = getExamID(exam_data);
                    EnrolledExam newExam = new EnrolledExam(examID,
                            exam_name, dateStart, description, code,
                            building, room, reserved, teachers, "");

                    exams.add(newExam);
                }

                JSONArray array = new JSONArray();
                if (exams.size() > 0)
                    array = UserUtils.enrolledExamsToJson(exams);

                if (array != null)
                    return array;
                else
                    return new JSONArray();
            }
        } catch (SocketTimeoutException e){
            Log.i(SyncUtilsEnrolled.REMINDER_JOB_TAG, "SOCKET_TIMEOUT");
        } catch (Exception e) {
            Log.e(SyncUtilsEnrolled.REMINDER_JOB_TAG, e.getMessage());
            Utils.saveBugReport(e);
        }

        return null;
    }

    private static ArrayList<String> getTeachers(Elements rows) {
        ArrayList<String> teachers = new ArrayList<>();

        int i, j = -1;
        for (i=0; i<rows.size(); i++){
            if (rows.get(i).text().toLowerCase().contains("docenti")) {
                Elements tmp = rows.get(i).children();
                for (j=0; j<tmp.size(); j++)
                    if (tmp.get(j).text().toLowerCase().contains("docenti"))
                        break;
            }
            if (j >= 0)
                break;
        }

        teachers.add(rows.get(i+2).child(j).text());
        if (j >= 0)
            for(i=j+1; i<rows.size(); i++)
                teachers.add(rows.get(i).text());

        return teachers;
    }

    private static ExamID getExamID(Element exam_data) {
        Element printForm = exam_data
                .select("td[title='stampa promemoria della prenotazione'] form")
                .first();

        if (printForm != null) {
            Element cds_esa_id = printForm
                    .select("input[name='" + CDS_ESA_ID + "']")
                    .first();

            Element app_id = printForm
                    .select("input[name='" + APP_ID + "']")
                    .first();

            Element att_did_esa_id = printForm
                    .select("input[name='" + ATT_DID_ESA_ID + "']")
                    .first();

            Element adsce_id = printForm
                    .select("input[name='" + ADSCE_ID + "']")
                    .first();

            if (cds_esa_id != null && app_id != null && att_did_esa_id != null && adsce_id != null)
                return new ExamID(
                        app_id.attr("value"),
                        cds_esa_id.attr("value"),
                        att_did_esa_id.attr("value"),
                        adsce_id.attr("value"));
            else
                return null;
        }

        return null;
    }



    private static Bundle tryGetUrlWithLogin(String url, User user, Context context)
            throws CertificateException, NoSuchAlgorithmException, IOException,
            KeyManagementException, KeyStoreException, NoSuchProviderException {
        Bundle result = new Bundle();

        // Try to get the private page
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html");
        HttpsURLConnection response = S3Helper.getPage(user, url, headers, context);

        int s3_response = response.getResponseCode();
        String html = null;
        if (s3_response == HttpURLConnection.HTTP_OK) {
            try {
                html = getHTML(response.getInputStream());
            } catch (FileNotFoundException e) {
                html = null;
            }
        }

        if (html != null)
            result.putString(PARAM_KEY_HTML, html);
        result.putInt(PARAM_KEY_RESPONSE, s3_response);

        return result;
    }

    private static ExamID getExamIdFromUrl(String url) {
        String tmp = url.substring(url.indexOf("?") + 1);
        String extraInfo[] = tmp.split("&");

        String app_id = null, cds_esa_id = null, adsce_id = null, att_did_esa_id = null;
        for (String str : extraInfo) {
            String name = str.substring(0, str.indexOf("="));
            switch (name){
                case CDS_ESA_ID:
                    cds_esa_id = str.substring(str.indexOf("=") + 1);
                    break;

                case APP_ID:
                    app_id = str.substring(str.indexOf("=") + 1);
                    break;

                case ADSCE_ID:
                    adsce_id = str.substring(str.indexOf("=") + 1);
                    break;

                case ATT_DID_ESA_ID:
                    att_did_esa_id = str.substring(str.indexOf("=") + 1);
                    break;
            }
        }

        if (!TextUtils.isEmpty(app_id) && !TextUtils.isEmpty(cds_esa_id) &&
                !TextUtils.isEmpty(att_did_esa_id) && !TextUtils.isEmpty(adsce_id))
            return new ExamID(app_id, cds_esa_id, att_did_esa_id, adsce_id);
        else
            return null;
    }

}
