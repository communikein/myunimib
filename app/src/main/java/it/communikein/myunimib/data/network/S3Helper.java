package it.communikein.myunimib.data.network;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.EnrolledExam;
import it.communikein.myunimib.data.database.Exam;
import it.communikein.myunimib.utilities.InjectorUtils;
import it.communikein.myunimib.utilities.UserUtils;
import it.communikein.myunimib.utilities.Utils;
import it.communikein.myunimib.data.User;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class S3Helper {

    private static final String TAG = S3Helper.class.getSimpleName();

    private static final String URL_HOME =
            "https://s3w.si.unimib.it/esse3/Home.do;";
    static final String URL_LIBRETTO =
            "https://s3w.si.unimib.it/esse3/auth/studente/Libretto/LibrettoHome.do;";
    static final String URL_AVAILABLE_EXAMS =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/Appelli.do;";
    static final String URL_ENROLLED_EXAMS =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/BachecaPrenotazioni.do;";
    private static final String URL_ENROLLED_EXAM_CERTIFICATE =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/StampaStatinoPDF.do?";
    static final String URL_ENROLL_TO =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/EffettuaPrenotazioneAppello.do;";
    static final String URL_UNENROLL_FROM =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/CancellaAppello.do;";
    public static final String URL_PROFILE_PICTURE =
            "https://s3w.si.unimib.it/esse3/auth/AddressBook/DownloadFoto.do;";
    public static final String URL_CAREER_BASE =
            "https://s3w.si.unimib.it/esse3/auth/studente/SceltaCarrieraStudente.do;";
    static final String URL_LOGOUT =
            "https://s3w.si.unimib.it/esse3/Logout.do;";

    private static final int ERROR_GENERIC = -1;
    public static final int ERROR_S3_NOT_AVAILABLE = -2;
    public static final int ERROR_WRONG_PASSWORD = -3;
    public static final int ERROR_CONNECTION_TIMEOUT = -4;
    public static final int ERROR_FACULTY_TO_CHOOSE = -5;
    private static final int ERROR_CAREER_OVER = -6;
    private static final int ERROR_RESPONSE_NULL = -7;

    public static final int OK_LOGGED_IN = 1;
    private static final int OK_LOGGED_OUT = 2;
    public static final int OK_UPDATED = 3;


    private static URL buildUrl(User user, String url, String query, boolean queryOperator) {
        String url_string = url + "JSESSIONID=" + user.getSessionID();
        if (user.isFacultyChosen())
            url_string += "?stu_id=" + user.getSelectedFaculty();
        if (query != null) {
            if (queryOperator) url_string += "?";
            else url_string += "&";
            url_string += query;
        }

        try {
            return new URL(url_string);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Nullable
    static SSLSocketFactory getSocketFactory(Context context) {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca;
            try (InputStream caInput = context.getResources().openRawResource(R.raw.terenasslca3)) {
                ca = cf.generateCertificate(caInput);
                Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
            }

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            HostnameVerifier hostnameVerifier = (hostname, session) -> {
                Log.e("CipherUsed", session.getCipherSuite());
                return hostname.compareTo("s3w.si.unimib.it") == 0;
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            return sslContext.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return  null;
    }

    static HttpsURLConnection getPage(User user, String url, String query, boolean queryOperator,
                                      Context context) throws IOException {
        String USER_AGENT = System.getProperty("http.agent");

        URL url_target = buildUrl(user, url, query, queryOperator);
        if (url_target == null) return null;
        HttpsURLConnection con = (HttpsURLConnection) url_target.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "text/html");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Host", "s3w.si.unimib.it");
        con.setRequestProperty("Connection", "keep-alive");
        if (user.getAuthToken() != null)
            con.setRequestProperty("Authorization", "Basic " + user.getAuthToken());
        if (user.getSessionID() != null)
            con.setRequestProperty("Cookie", "JSESSIONID=" + user.getSessionID());
        con.setConnectTimeout(5000);
        con.setSSLSocketFactory(getSocketFactory(context));

        String cookie = con.getHeaderField("Set-Cookie");
        if (cookie != null && !cookie.isEmpty()) {
            user = UserUtils.updateSessionId(user, cookie, context);
            con = getPage(user, url, query, queryOperator, context);
        }

        return con;
    }

    static String getHTML(InputStream instream) throws IOException {
        BufferedReader rd;
        String ris;
        rd = new BufferedReader(new InputStreamReader(instream));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        ris = result.toString();

        return ris;
    }



    private static boolean isCareerOver(User user, Document document) {
        if (user.isFake()) return false;

        Element el = document.getElementById("gu-textStatusStudente");

        return el.text().toLowerCase().contains("cessato");
    }

    private static User downloadUserData(User user, Context context) throws IOException {
        if (user.isFake()) {
            user.setMatricola("293640");
            user.setName("Pippo Pluto");
            user.setTotalCFU(42);
            user.setAverageMark(24);

            return user;
        }

        try {
            HttpURLConnection response = S3Helper.getPage(user, S3Helper.URL_LIBRETTO, null,
                    false, context);
            Document doc;
            String result = S3Helper.getHTML(response.getInputStream());

            if (response.getResponseCode() == HttpURLConnection.HTTP_OK){
                doc = Jsoup.parse(result);
                Element el2 = doc.select("#esse3old > table:has(div.titolopagina)").first();

                if (el2 != null) {
                    String matricola = el2.text();
                    matricola = matricola.substring(matricola.indexOf("MAT. ") + 5);
                    matricola = matricola.substring(0, 6);

                    user.setMatricola(matricola);
                    Log.d("LOGIN_USER_DATA", "Matricola: " + matricola);
                }
            }

            response = S3Helper.getPage(user, S3Helper.URL_HOME, null,
                    false, context);
            result = S3Helper.getHTML(response.getInputStream());
            doc = Jsoup.parse(result);
            Element el = doc.select("div#sottotitolo-menu-principale").first();

            String name = el.text();
            user.setName(name);
            Log.d("LOGIN_USER_DATA", "Name: " + name);

            Elements els = doc.select("div#gu-boxRiepilogoEsami dl.record-riga dd");
            String averageTmp = els.get(2).text();
            averageTmp = averageTmp.substring(0, averageTmp.length()-3);
            String cfuTmp = els.get(3).text();
            cfuTmp = cfuTmp.substring(0, cfuTmp.indexOf("/"));
            float averageMark = Float.parseFloat(averageTmp);
            int totalCfu = Integer.parseInt(cfuTmp);
            user.setTotalCFU(totalCfu);
            user.setAverageMark(averageMark);

            Log.d("LOGIN_USER_DATA", "Average mark: " + averageTmp);
            Log.d("LOGIN_USER_DATA", "CFU: " + cfuTmp);

            return user;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e){
            Utils.saveBugReport(e, TAG);

            throw e;
        }
    }

    private static SparseArray<String> hasMultiFaculty(User user, Document document) {
        if (user.isFake()) return downloadFacultiesList(user, null);

        Element el1 = document.select("#titolo-menu-principale").first();
        boolean hasMultiFaculty = el1.text().toLowerCase().equals("registrato");

        if (hasMultiFaculty)
            return downloadFacultiesList(user, document);
        else
            return null;
    }

    private static SparseArray<String> downloadFacultiesList(User user, Document document) {
        SparseArray<String> courses = new SparseArray<>();

        if (user.isFake()) {
            courses.put(34829, "Faculty of IT");
            courses.put(19347, "Faculty of Science");
            courses.put(58240, "Faculty of Chemistry");

            return courses;
        }

        Elements els = document.select("table#gu_table_sceltacarriera tbody tr");
        if (!els.isEmpty()) {
            for (Element el : els) {
                String name = el.child(1).text() + " in " + el.child(2).text();
                String relativeUrl = el.select("#gu_toolbar_sceltacarriera a")
                        .attr("href");
                relativeUrl = relativeUrl.substring(relativeUrl.indexOf("?stu_id=") + 8);
                int stu_id = Integer.parseInt(relativeUrl);

                courses.put(stu_id, name);
            }
        }

        return courses;
    }

    private static boolean downloadCertificate(User user, Exam exam, Context context) throws IOException {
        String urlParameters = exam.examIdToUrl();
        HttpsURLConnection connection = getPage(user,
                URL_ENROLLED_EXAM_CERTIFICATE, urlParameters, false, context);
        InputStream ris = null;

        if (connection != null)
            ris = connection.getInputStream();

        if (ris != null) {
            try {
                File file = exam.getCertificatePath();
                FileOutputStream f = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int len;

                while ((len = ris.read(buffer)) > 0)
                    f.write(buffer, 0, len);
                f.close();

                return true;
            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }



    public static class LoginLoader extends AsyncTaskLoader<User> {

        static final String TAG = LoginLoader.class.getSimpleName();

        public static final String RESULT = "RESULT";

        // Weak references will still allow the Context to be garbage-collected
        private final WeakReference<Activity> mActivity;

        private User mUser;

        public LoginLoader(Activity activity, User user) {
            super(activity);

            this.mActivity = new WeakReference<>(activity);
            this.mUser = user;
        }

        @Override
        public User loadInBackground() {
            Log.d(TAG, "STARTED");

            /* Get the context from the activity, if null end the login process */
            Context context = mActivity.get();
            if (context == null) return null;

            int loggedIn;
            try {
                /* Try logging in the user */
                Bundle result = doLogin(mUser, context);
                loggedIn = result.getInt(RESULT);

                String sessionId = result.getString(User.PREF_SESSION_ID);
                String facultiesString = result.getString(User.PREF_FACULTIES);

                if (sessionId != null)
                    mUser.setSessionID(sessionId);
                if (facultiesString != null) {
                    JSONObject facultiesJson = new JSONObject(facultiesString);

                    mUser.setFaculties(facultiesJson);
                }
                if (sessionId != null || facultiesString != null)
                    UserUtils.saveUser(mUser, context);


                /*
                 * If the server recognise the user as logged in, download the user's data and
                 * notify the user that the login process is completed successfully.
                 */
                if (loggedIn == OK_LOGGED_IN) {
                    Log.d(TAG, "RESULT: LOGIN COMPLETED. (" + loggedIn + ")");

                    Log.d(TAG, "Downloading user data");
                    mUser = downloadUserData(mUser, context);
                    UserUtils.saveUser(mUser, context);
                }
            } catch (SocketTimeoutException e){
                loggedIn = ERROR_CONNECTION_TIMEOUT;
            } catch (Exception e) {
                loggedIn = ERROR_GENERIC;
                Utils.saveBugReport(e, TAG);
            }

            mUser.setTag(loggedIn);

            return mUser;
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        public static Bundle doLogin(User user, Context context) throws SocketTimeoutException {
            Bundle result = new Bundle();
            int ris;

            try {
                int respCode;
                HttpsURLConnection resp = null;

                /* If this is a real login, try to contact the server and save its response */
                if (!user.isFake()) {
                    resp = getPage(user, URL_LIBRETTO, null, false, context);
                    if (resp != null)
                        respCode = resp.getResponseCode();
                    else
                        respCode = ERROR_RESPONSE_NULL;
                }
                /* Else, pretend the connection was OK */
                else {
                    respCode = HttpURLConnection.HTTP_OK;
                }

                /* Now process the server response, and act accordingly. */
                /* 1. The server is not available. End the process and notify the user. */
                if (respCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    ris = ERROR_S3_NOT_AVAILABLE;
                    Log.e(TAG, "S3 not available.");
                }
                /* 2. The server is available. Proceed with the login process. */
                else {
                    Log.d(TAG, "S3 available.");
                    Document document = null;

                    /*
                     * If this is a real login, check for a new JSESSIONID from the server, if
                     * I have one, it means that the server did not authenticate the user yet.
                     * Hence, update the user's session ID and ask for the same page again.
                     * Finally, get the server's response HTML and parse it.
                     */
                    if (!user.isFake()) {
                        /* Check for a new session ID from the server */
                        if (resp != null && resp.getHeaderField("Set-Cookie") != null) {
                            /* Update the user's session ID */
                            String jsessionid = resp.getHeaderField("Set-Cookie");
                            user.setSessionID(jsessionid);

                            result.putString(User.PREF_SESSION_ID, jsessionid);
                            //user = UserUtils.updateSessionId(user, jsessionid, context);

                            Log.d(TAG, "Got new JSESSIONID");

                            /* Try to get the page with the new session ID */
                            resp = getPage(user, URL_LIBRETTO, null, false, context);
                            if (resp != null)
                                respCode = resp.getResponseCode();
                            else
                                respCode = ERROR_RESPONSE_NULL;
                        }
                        Log.d(TAG, "SESSION ID = " + user.getSessionID());
                        Log.d(TAG, "AUTH TOKEN = " + user.getAuthToken());

                        /* Get and parse the HTML from the response */
                        if (resp != null) {
                            try {
                                String html = getHTML(resp.getInputStream());
                                document = Jsoup.parse(html);
                            } catch (IOException e) {
                                document = null;
                            }
                        }
                    }

                    /* If the server recognise the user as logged-in */
                    if (respCode == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "User logged in.");

                        /*
                         * If the user only has one faculty, check the user's career status
                         * and end the login process with either one of the following outcomes:
                         * - The career is over. The user has not successfully logged in;
                         * - The career is not over. The user has successfully logged in.
                         */
                        if (user.hasOneFaculty()) {
                            if (!isCareerOver(user, document)) {
                                Log.d(TAG, "FINISHED - OK.");
                                ris = OK_LOGGED_IN;
                            } else {
                                Log.d(TAG, "FINISHED - CAREER OVER.");
                                ris = ERROR_CAREER_OVER;
                            }
                        }

                        /* If the user may have more faculties. */
                        else {
                            Log.d(TAG, "User might have multiple faculties.");

                            /*
                             * If the app has the list of faculty to choose, but the user
                             * hasn't chosen one yet, notify the app that the user needs to
                             * choose the faculty.
                             */
                            if (user.shouldChooseFaculty()) {
                                Log.d(TAG, "User needs to choose the faculty.");
                                ris = ERROR_FACULTY_TO_CHOOSE;
                            }

                            /* If the user has chosen the faculty, end the login process */
                            else if (user.isFacultyChosen()) {
                                Log.d(TAG, "User has chosen the faculty.");
                                ris = OK_LOGGED_IN;
                            }

                            /*
                             * If the app doesn't know if the user has multiple faculties,
                             * hence the app doesn't have the list of faculties.
                             */
                            else {
                                /* Try to get the user list of faculties */
                                SparseArray<String> faculties = hasMultiFaculty(user, document);

                                /* If the user has only one faculty */
                                if (faculties == null) {
                                    Log.d(TAG, "ONLY ONE FACULTY.");
                                    ris = OK_LOGGED_IN;
                                }

                                /* If the user has multiple faculties */
                                else {
                                    Log.d(TAG, "Multiple faculties found.");
                                    for (int i=0; i<faculties.size(); i++)
                                        Log.d(TAG, "Faculty: " + faculties.valueAt(i));

                                    /* Set the user's faculties and save it. */
                                    user.setFaculties(faculties);

                                    result.putString(User.PREF_FACULTIES, user.getFacultiesJSON().toString());
                                    //UserUtils.saveUser(user, context);

                                    ris = ERROR_FACULTY_TO_CHOOSE;
                                }
                            }
                        }
                    }

                    /* Otherwise the the password is not valid */
                    else {
                        Log.d(TAG, "WRONG PASSWORD.");
                        ris = ERROR_WRONG_PASSWORD;
                    }
                }
            } catch (SocketTimeoutException e){
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                ris = ERROR_GENERIC;
            }

            result.putInt(RESULT, ris);
            return result;
            // return ris;
        }

    }

    public static class UserDataLoader extends AsyncTaskLoader<User> {

        static final String TAG = UserDataLoader.class.getSimpleName();

        // Weak references will still allow the Context to be garbage-collected
        private final WeakReference<Activity> mActivity;

        private User mUser;

        public UserDataLoader(Activity activity, User user) {
            super(activity);

            this.mActivity = new WeakReference<>(activity);
            this.mUser = user;
        }

        @Override
        public User loadInBackground() {
            Log.d(TAG, "STARTED");

            /* Get the context from the activity, if null end the login process */
            Context context = mActivity.get();
            if (context == null) return null;

            int loggedIn;
            try {
                Log.d(TAG, "Downloading user data");

                mUser = downloadUserData(mUser, context);
                UserUtils.saveUser(mUser, context);
                loggedIn = OK_UPDATED;
            } catch (SocketTimeoutException e){
                loggedIn = ERROR_CONNECTION_TIMEOUT;
            } catch (IOException e) {
                loggedIn = ERROR_GENERIC;
                Utils.saveBugReport(e, TAG);
            }

            mUser.setTag(loggedIn);

            return mUser;
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }
    }

    public static class LogoutLoader extends AsyncTaskLoader<User> {

        // Weak references will still allow the Context to be garbage-collected
        private final WeakReference<Activity> mActivity;
        private final User mUser;

        public LogoutLoader(Activity activity, User user) {
            super(activity);

            this.mActivity = new WeakReference<>(activity);
            this.mUser = user;
        }

        @Override
        public User loadInBackground() {
            Activity context = mActivity.get();
            if (context == null) return null;

            try {
                if (!mUser.isFake())
                    getPage(mUser, URL_LOGOUT, null, false, context);
                UserUtils.removeUser(context);

                mUser.setTag(OK_LOGGED_OUT);
            } catch (Exception e) {
                return null;
            }

            return mUser;
        }
    }

    public static class CertificateLoader extends AsyncTaskLoader<Cursor> {

        // Weak references will still allow the Context to be garbage-collected
        private final WeakReference<Activity> mActivity;
        private User mUser;
        private final EnrolledExam mExam;

        public CertificateLoader(Activity activity, EnrolledExam exam) {
            super(activity);

            this.mActivity = new WeakReference<>(activity);
            this.mExam = exam;
        }

        @Override
        public Cursor loadInBackground() {
            Activity context = mActivity.get();
            if (context == null) return null;

            mUser = UserUtils.getUser(context);

            try {
                downloadCertificate(mUser, mExam, context);
                return null;
            } catch (SocketTimeoutException e) {
                return null;
            } catch (IOException e) {
                Utils.saveBugReport(e, TAG);
            }

            return null;
        }
    }

    public static class EnrollLoader extends AsyncTaskLoader<Boolean> {

        public static final int STATUS_ERROR_QUESTIONNAIRE_TO_FILL = -1;
        public static final int STATUS_ERROR_CERTIFICATE = -2;
        public static final int STATUS_ERROR_GENERAL = -3;

        public static final int STATUS_STARTED = 1;
        public static final int STATUS_ENROLLMENT_OK = 2;
        public static final int STATUS_CERTIFICATE_DOWNLOADED = 3;

        // Weak references will still allow the Context to be garbage-collected
        private final WeakReference<Activity> mActivity;
        private User mUser;
        private final Exam mExam;
        private final EnrollUpdatesListener mEnrollUpdatesListener;

        public interface EnrollUpdatesListener {
            void onEnrollmentUpdate(int status);
        }

        public EnrollLoader(Activity activity, Exam exam, EnrollUpdatesListener listener) {
            super(activity);

            this.mActivity = new WeakReference<>(activity);
            this.mExam = exam;
            this.mEnrollUpdatesListener = listener;
        }

        @Override
        public Boolean loadInBackground() {
            Activity context = mActivity.get();
            if (context == null) return null;

            mUser = UserUtils.getUser(context);
            String urlParameters = mExam.examIdToUrl();

            try {
                mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_STARTED);

                Bundle result = UnimibNetworkDataSource
                        .tryGetUrlWithLogin(URL_ENROLL_TO, mUser, urlParameters, true, context);
                int s3_response = result.getInt(UnimibNetworkDataSource.PARAM_KEY_RESPONSE);
                String html = result.getString(UnimibNetworkDataSource.PARAM_KEY_HTML);

                Document doc = Jsoup.parse(html);
                Element element = doc.select("#app-text_esito_pren_msg").first();
                String responseText = element.text().toLowerCase();

                if (!TextUtils.isEmpty(responseText) &&
                        responseText.contains("non risulta compilato il questionario")) {
                    mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_ERROR_QUESTIONNAIRE_TO_FILL);
                }
                else if (responseText.equals("") && s3_response == HttpURLConnection.HTTP_OK){
                    mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_ENROLLMENT_OK);

                    InjectorUtils.provideRepository(context).deleteAvailableExam(mExam);
                    InjectorUtils.provideNetworkDataSource(context)
                            .startFetchEnrolledExamsService();
                    InjectorUtils.provideNetworkDataSource(context)
                            .startFetchAvailableExamsService();

                    boolean downloaded = downloadCertificate(mUser, mExam, context);
                    if (downloaded) {
                        boolean certFound = mExam.getCertificatePath().exists();
                        if (certFound)
                            mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_CERTIFICATE_DOWNLOADED);
                    }
                    else
                        mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_ERROR_CERTIFICATE);

                    return true;
                }

            } catch (SocketTimeoutException e) {
                mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_ERROR_GENERAL);
            } catch (IOException e) {
                mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_ERROR_GENERAL);
                Utils.saveBugReport(e, TAG);
            }

            return false;
        }


    }
    
}