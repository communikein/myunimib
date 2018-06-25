package it.communikein.myunimib.data.network;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.model.Exam;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.data.network.loaders.CertificateLoader;
import it.communikein.myunimib.data.network.loaders.EnrollLoader;
import it.communikein.myunimib.data.network.loaders.LoginLoader;
import it.communikein.myunimib.data.network.loaders.S3Helper;
import it.communikein.myunimib.data.network.loaders.UnEnrollLoader;
import it.communikein.myunimib.data.network.loaders.UserDataLoader;
import it.communikein.myunimib.utilities.DateHelper;
import it.communikein.myunimib.utilities.Utils;

import static it.communikein.myunimib.data.network.loaders.S3Helper.getHTML;

@Singleton
public class UnimibNetworkDataSource {

    private static final String LOG_TAG = UnimibNetworkDataSource.class.getSimpleName();

    public static class WebpageDownloadException extends Exception {
        String htmlSource;

        WebpageDownloadException(String message, String html){
            super(message);
            this.htmlSource = html;
        }

        public String getHtmlSource() {
            return this.htmlSource;
        }
    }

    /*
     * Interval at which to sync with the data. Use TimeUnit for convenience, rather than
     * writing out a bunch of multiplication ourselves and risk making a silly mistake.
     */
    private static final int SYNC_INTERVAL_MINUTES = 30;
    private static final int SYNC_INTERVAL_SECONDS =
            (int) TimeUnit.MINUTES.toSeconds(SYNC_INTERVAL_MINUTES);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static final String UNIMIB_BOOKLET_SYNC_TAG = "unimib-booklet-sync";
    private static final String UNIMIB_AVAILABLE_SYNC_TAG = "unimib-available-sync";
    private static final String UNIMIB_ENROLLED_SYNC_TAG = "unimib-enrolled-sync";

    public static final String PARAM_KEY_HTML = "param-key-html";
    public static final String PARAM_KEY_RESPONSE = "param-key-response";

    public static final String CDS_ESA_ID = "CDS_ESA_ID";
    public static final String ATT_DID_ESA_ID = "ATT_DID_ESA_ID";
    public static final String APP_ID = "APP_ID";
    public static final String ADSCE_ID = "ADSCE_ID";

    private final Context mContext;
    private final AppExecutors mExecutors;

    private final MutableLiveData<User> mUser;
    private final MutableLiveData<List<BookletEntry>> mDownloadedBooklet;
    private final MutableLiveData<List<AvailableExam>> mDownloadedAvailableExams;
    private final MutableLiveData<List<EnrolledExam>> mDownloadedEnrolledExams;

    private final MutableLiveData<Boolean> mUserLoading;
    private final MutableLiveData<Boolean> mBookletLoading;
    private final MutableLiveData<Boolean> mAvailableExamsLoading;
    private final MutableLiveData<Boolean> mEnrolledExamsLoading;

    @Inject
    public UnimibNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;

        mDownloadedBooklet = new MutableLiveData<>();
        mDownloadedAvailableExams = new MutableLiveData<>();
        mDownloadedEnrolledExams = new MutableLiveData<>();
        mUser = new MutableLiveData<>();

        mBookletLoading = new MutableLiveData<>();
        mAvailableExamsLoading = new MutableLiveData<>();
        mEnrolledExamsLoading = new MutableLiveData<>();
        mUserLoading = new MutableLiveData<>();
    }



    /***************
     * BOOKLET *****
     ***************/

    public LiveData<List<BookletEntry>> getOnlineBooklet() {
        return mDownloadedBooklet;
    }

    public LiveData<Boolean> getBookletLoading() {
        return mBookletLoading;
    }

    public void scheduleRecurringFetchBookletSync() {
        Driver driver = new GooglePlayDriver(mContext);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // Create the Job to periodically sync Sunshine
        Job syncSunshineJob = dispatcher.newJobBuilder()
                /* The Service that will be used to sync Sunshine's data */
                .setService(BookletSyncJobService.class)
                /* Set the UNIQUE tag used to identify this Job */
                .setTag(UNIMIB_BOOKLET_SYNC_TAG)
                /*
                 * Network constraints on which this Job should run. We choose to run on any
                 * network, but you can also choose to run only on un-metered networks or when the
                 * device is charging. It might be a good idea to include a preference for this,
                 * as some users may not want to download any data on their mobile plan. ($$$)
                 */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                /*
                 * setLifetime sets how long this job should persist. The options are to keep the
                 * Job "forever" or to have it die the next time the device boots up.
                 */
                .setLifetime(Lifetime.FOREVER)
                /*
                 * We want Sunshine's weather data to stay up to date, so we tell this Job to recur.
                 */
                .setRecurring(true)
                /*
                 * We want the weather data to be synced every 3 to 4 hours. The first argument for
                 * Trigger's static executionWindow method is the start of the time frame when the
                 * sync should be performed. The second argument is the latest point in time at
                 * which the data should be synced. Please note that this end time is not
                 * guaranteed, but is more of a guideline for FirebaseJobDispatcher to go off of.
                 */
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                /*
                 * If a Job with the tag provided already exists, this new job will replace
                 * the old one.
                 */
                .setReplaceCurrent(true)
                /* Once the Job is ready, call the builder's build method to return the Job */
                .build();

        // Schedule the Job with the dispatcher
        dispatcher.schedule(syncSunshineJob);
        Log.d(LOG_TAG, "Booklet job scheduled");
    }

    public void fetchBooklet(final User user, S3Helper.NewSessionIdListener listener) {
        // Notify any observer that the loading is started
        mBookletLoading.postValue(true);

        mExecutors.networkIO().execute(() -> {
            // Get the latest booklet from S3
            ArrayList<BookletEntry> response = downloadBooklet(user, mContext, listener);

            // As long as the booklet is not empty, update the LiveData storing it.
            if (response != null && response.size() != 0)
                mDownloadedBooklet.postValue(response);

            // Notify any observer that the loading is completed
            mBookletLoading.postValue(false);
        });
    }

    private static ArrayList<BookletEntry> downloadBooklet(User user, Context context,
                                                           S3Helper.NewSessionIdListener listener) {
        Log.d(LOG_TAG, "Booklet download started.");
        if (context == null)
            return null;

        String html;
        try {
            /* Try to get the private page */
            Bundle result = authenticatedGET(S3Helper.URL_LIBRETTO, user, null,
                    false, context, listener);

            html = result.getString(PARAM_KEY_HTML);
            int s3_response = result.getInt(PARAM_KEY_RESPONSE);

            /* If it fails the first time, try once again */
            if (html == null || s3_response != HttpURLConnection.HTTP_OK) {
                result = authenticatedGET(S3Helper.URL_LIBRETTO, user, null,
                        false, context, listener);

                html = result.getString(PARAM_KEY_HTML);
                s3_response = result.getInt(PARAM_KEY_RESPONSE);
            }

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
                        dateStart = DateHelper.date.parse(date);
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
                            cfu, state, mark, code, false);
                    booklet.add(newExam);
                }

                Log.d(LOG_TAG, "Booklet download completed. List size: " + booklet.size());

                return booklet;
            }

            String error_message = "Download completed - booklet (S3 - " + s3_response + "). ";
            throw new WebpageDownloadException(error_message, html);
        } catch (WebpageDownloadException e) {
            Utils.saveBugReport(e, LOG_TAG, "downloadBooklet",
                    e.getHtmlSource());

            return null;
        } catch (Exception e) {
            Utils.saveBugReport(e, LOG_TAG, "downloadBooklet");

            return null;
        }
    }



    /***********************
     * AVAILABLE EXAMS *****
     ***********************/

    public LiveData<List<AvailableExam>> getOnlineAvailableExams() {
        return mDownloadedAvailableExams;
    }

    public LiveData<Boolean> getAvailableExamsLoading() {
        return mAvailableExamsLoading;
    }

    public void scheduleRecurringFetchAvailableExamsSync() {
        Driver driver = new GooglePlayDriver(mContext);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // Create the Job to periodically sync Sunshine
        Job syncSunshineJob = dispatcher.newJobBuilder()
                /* The Service that will be used to sync Sunshine's data */
                .setService(ExamAvailableSyncJobService.class)
                /* Set the UNIQUE tag used to identify this Job */
                .setTag(UNIMIB_AVAILABLE_SYNC_TAG)
                /*
                 * Network constraints on which this Job should run. We choose to run on any
                 * network, but you can also choose to run only on un-metered networks or when the
                 * device is charging. It might be a good idea to include a preference for this,
                 * as some users may not want to download any data on their mobile plan. ($$$)
                 */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                /*
                 * setLifetime sets how long this job should persist. The options are to keep the
                 * Job "forever" or to have it die the next time the device boots up.
                 */
                .setLifetime(Lifetime.FOREVER)
                /*
                 * We want Sunshine's weather data to stay up to date, so we tell this Job to recur.
                 */
                .setRecurring(true)
                /*
                 * We want the weather data to be synced every 3 to 4 hours. The first argument for
                 * Trigger's static executionWindow method is the start of the time frame when the
                 * sync should be performed. The second argument is the latest point in time at
                 * which the data should be synced. Please note that this end time is not
                 * guaranteed, but is more of a guideline for FirebaseJobDispatcher to go off of.
                 */
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                /*
                 * If a Job with the tag provided already exists, this new job will replace
                 * the old one.
                 */
                .setReplaceCurrent(true)
                /* Once the Job is ready, call the builder's build method to return the Job */
                .build();

        // Schedule the Job with the dispatcher
        dispatcher.schedule(syncSunshineJob);
        Log.d(LOG_TAG, "Available exams job scheduled");
    }

    public void fetchAvailableExams(final User user, S3Helper.NewSessionIdListener listener) {
        // Notify any observer that the loading is started
        mAvailableExamsLoading.postValue(true);

        mExecutors.networkIO().execute(() -> {
            // Get the latest available exams from S3
            ArrayList<AvailableExam> response = downloadAvailableExams(user, mContext, listener);

            // As long as there are available exams, update the LiveData storing them.
            if (response != null && response.size() != 0)
                mDownloadedAvailableExams.postValue(response);

            // Notify any observer that the loading is completed
            mAvailableExamsLoading.postValue(false);
        });
    }

    private static ArrayList<AvailableExam> downloadAvailableExams(User user, Context context,
                                                                   S3Helper.NewSessionIdListener listener) {
        Log.d(LOG_TAG, "Available exams download started.");

        if (context == null)
            return null;

        String html = null;
        try {
            /* Try to get the private page */
            Bundle result = authenticatedGET(S3Helper.URL_AVAILABLE_EXAMS, user, null,
                    false, context, listener);

            html = result.getString(PARAM_KEY_HTML);
            int s3_response = result.getInt(PARAM_KEY_RESPONSE);

            /* If it fails the first time, try once again */
            if (html == null || s3_response != HttpURLConnection.HTTP_OK) {
                result = authenticatedGET(S3Helper.URL_AVAILABLE_EXAMS, user, null,
                        false, context, listener);

                html = result.getString(PARAM_KEY_HTML);
                s3_response = result.getInt(PARAM_KEY_RESPONSE);
            }

            if (html != null && s3_response == HttpURLConnection.HTTP_OK) {
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

                    Date date = DateHelper.date.parse(date_str);
                    Date enrollment_window_begin = DateHelper.date
                            .parse(enrollment_window_str[0]);
                    Date enrollment_window_end = DateHelper.date
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

                Log.d(LOG_TAG, "Available exams download completed. List size: " + exams.size());

                return exams;
            }

            String error_message = "Download completed - availableExams (S3 - " + s3_response + "). ";
            throw new WebpageDownloadException(error_message, html);
        } catch (WebpageDownloadException e) {
            Utils.saveBugReport(e, LOG_TAG, "downloadAvailableExams",
                    html);

            return null;
        } catch (Exception e) {
            Utils.saveBugReport(e, LOG_TAG, "downloadAvailableExams");
            return null;
        }
    }

    public EnrollLoader enrollExam(Exam exam, Activity activity,
                                   EnrollLoader.EnrollCompleteListener enrollCompleteListener,
                                   EnrollLoader.EnrollUpdatesListener enrollUpdatesListener) {
        return S3Helper.createEnrollLoader(exam, activity,
                enrollCompleteListener, enrollUpdatesListener);
    }



    /**********************
     * ENROLLED EXAMS *****
     **********************/

    public LiveData<List<EnrolledExam>> getOnlineEnrolledExams() {
        return mDownloadedEnrolledExams;
    }

    public LiveData<Boolean> getEnrolledExamsLoading() {
        return mEnrolledExamsLoading;
    }

    public void scheduleRecurringFetchEnrolledExamsSync() {
        Driver driver = new GooglePlayDriver(mContext);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // Create the Job to periodically sync Sunshine
        Job syncSunshineJob = dispatcher.newJobBuilder()
                /* The Service that will be used to sync Sunshine's data */
                .setService(ExamEnrolledSyncJobService.class)
                /* Set the UNIQUE tag used to identify this Job */
                .setTag(UNIMIB_ENROLLED_SYNC_TAG)
                /*
                 * Network constraints on which this Job should run. We choose to run on any
                 * network, but you can also choose to run only on un-metered networks or when the
                 * device is charging. It might be a good idea to include a preference for this,
                 * as some users may not want to download any data on their mobile plan. ($$$)
                 */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                /*
                 * setLifetime sets how long this job should persist. The options are to keep the
                 * Job "forever" or to have it die the next time the device boots up.
                 */
                .setLifetime(Lifetime.FOREVER)
                /*
                 * We want Sunshine's weather data to stay up to date, so we tell this Job to recur.
                 */
                .setRecurring(true)
                /*
                 * We want the weather data to be synced every 3 to 4 hours. The first argument for
                 * Trigger's static executionWindow method is the start of the time frame when the
                 * sync should be performed. The second argument is the latest point in time at
                 * which the data should be synced. Please note that this end time is not
                 * guaranteed, but is more of a guideline for FirebaseJobDispatcher to go off of.
                 */
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                /*
                 * If a Job with the tag provided already exists, this new job will replace
                 * the old one.
                 */
                .setReplaceCurrent(true)
                /* Once the Job is ready, call the builder's build method to return the Job */
                .build();

        // Schedule the Job with the dispatcher
        dispatcher.schedule(syncSunshineJob);
        Log.d(LOG_TAG, "Enrolled exams job scheduled");
    }

    public void fetchEnrolledExams(final User user, S3Helper.NewSessionIdListener listener) {
        // Notify any observer that the loading is started
        mEnrolledExamsLoading.postValue(true);

        mExecutors.networkIO().execute(() -> {
            // Get the latest enrolled exams from S3
            ArrayList<EnrolledExam> response = downloadEnrolledExams(user, mContext, listener);

            // As long as there are enrolled exams, update the LiveData storing them.
            if (response != null && response.size() != 0)
                mDownloadedEnrolledExams.postValue(response);

            // Notify any observer that the loading is completed
            mEnrolledExamsLoading.postValue(false);
        });
    }

    private static ArrayList<EnrolledExam> downloadEnrolledExams(User user, Context context,
                                                                 S3Helper.NewSessionIdListener listener) {
        Log.d(LOG_TAG, "Enrolled exams download started.");

        if (context == null)
            return null;

        String html = null;
        try {
            /* Try to get the private page */
            Bundle result = authenticatedGET(S3Helper.URL_ENROLLED_EXAMS, user, null,
                    false, context, listener);

            html = result.getString(PARAM_KEY_HTML);
            int s3_response = result.getInt(PARAM_KEY_RESPONSE);

            /* If it fails the first time, try once again */
            if (html == null || s3_response != HttpURLConnection.HTTP_OK) {
                result = authenticatedGET(S3Helper.URL_ENROLLED_EXAMS, user, null,
                        false, context, listener);

                html = result.getString(PARAM_KEY_HTML);
                s3_response = result.getInt(PARAM_KEY_RESPONSE);
            }

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
                    Date dateStart = DateHelper.dateTime.parse(dateTimeTmp);

                    // Save the exams ID
                    ExamID examID = getExamIdFromDocument(exam_data);
                    EnrolledExam newExam = new EnrolledExam(examID,
                            exam_name, dateStart, description, code,
                            building, room, reserved, teachers);

                    exams.add(newExam);
                }

                Log.d(LOG_TAG, "Enrolled exams download completed. List size: " + exams.size());

                return exams;
            }

            String error_message = "Download completed - enrolledExams (S3 - " + s3_response + "). ";
            throw new WebpageDownloadException(error_message, html);
        } catch (WebpageDownloadException e) {
            Utils.saveBugReport(e, LOG_TAG, "downloadEnrolledExams",
                    html);
            return null;
        } catch (Exception e) {
            Utils.saveBugReport(e, LOG_TAG, "downloadEnrolledExams");
            return null;
        }
    }

    public CertificateLoader loadCertificate(EnrolledExam exam, Activity activity) {
        return S3Helper.createCertificateLoader(exam, activity);
    }

    public UnEnrollLoader unEnrollExam(Exam exam, Activity activity,
                                       UnEnrollLoader.UnEnrollCompleteListener unEnrollCompleteListener,
                                       UnEnrollLoader.UnEnrollUpdatesListener unEnrollUpdatesListener) {
        return S3Helper.createUnEnrollLoader(exam, activity,
                unEnrollCompleteListener, unEnrollUpdatesListener);
    }



    /*************
     * USERS *****
     *************/

    public LiveData<User> getOnlineUser() { return mUser; }

    public LiveData<Boolean> getUserLoading() {
        return mUserLoading;
    }

    public LoginLoader loginUser(User userToLogin, Activity activity) {
        return S3Helper.createLoginLoader(userToLogin, activity);
    }

    public UserDataLoader downloadUserData(Activity activity) {
        return S3Helper.createUserDataLoader(activity);
    }



    /***************
     * VARIOUS *****
     ***************/

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

    private static ExamID getExamIdFromDocument(Element exam_data) {
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


    public static Bundle authenticatedPOST(String url, UserAuthentication user, String params,
                                           boolean queryOperator, Context context,
                                           S3Helper.NewSessionIdListener listener) throws IOException {
        // Try to get the private page
        HttpsURLConnection response = S3Helper.postPage(user, url, params, queryOperator, context, listener);

        return parseServerResponse(response);
    }

    public static Bundle authenticatedGET(String url, UserAuthentication user, String params,
                                          boolean queryOperator, Context context,
                                          S3Helper.NewSessionIdListener listener) throws IOException {
        // Try to get the private page
        HttpsURLConnection response = S3Helper.getPage(user, url, params, queryOperator, context, listener);

        return parseServerResponse(response);
    }

    public static Bundle parseServerResponse(HttpsURLConnection response) throws IOException {
        Bundle result = new Bundle();

        if (response != null) {
            int s3_response = response.getResponseCode();
            String html = null;
            if (s3_response == HttpURLConnection.HTTP_OK) {
                try {
                    html = getHTML(response.getInputStream());
                } catch (FileNotFoundException e) {
                    html = null;
                }
            }

            if (!TextUtils.isEmpty(html)) {
                result.putString(PARAM_KEY_HTML, html);
                result.putInt(PARAM_KEY_RESPONSE, s3_response);
            }
            else
                result.putInt(PARAM_KEY_RESPONSE, -1);
        }

        return result;
    }

}
