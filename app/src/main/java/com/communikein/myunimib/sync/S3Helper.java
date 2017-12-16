package com.communikein.myunimib.sync;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.communikein.myunimib.R;
import com.communikein.myunimib.utilities.UserUtils;
import com.communikein.myunimib.utilities.Utils;
import com.communikein.myunimib.User;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class S3Helper {

    static final String URL_LIBRETTO =
            "https://s3w.si.unimib.it/esse3/auth/studente/Libretto/LibrettoHome.do;";
    private static final String URL_HOME =
            "https://s3w.si.unimib.it/esse3/Home.do;";
    private static final String URL_LOGIN =
            "https://s3w.si.unimib.it/esse3/auth/Logon.do;";
    private static final String URL_LOGOUT =
            "https://s3w.si.unimib.it/esse3/Logout.do;";
    static final String URL_AVAILABLE_EXAMS =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/Appelli.do;";
    static final String URL_ENROLLED_EXAMS =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/BachecaPrenotazioni.do;";
    public static final String URL_PROFILE_PICTURE =
            "https://s3w.si.unimib.it/esse3/auth/AddressBook/DownloadFoto.do;";
    public static final String URL_CAREER_BASE =
            "https://s3w.si.unimib.it/esse3/auth/studente/SceltaCarrieraStudente.do;";
    public static final String URL_CORTESIA =
            "http://www.si.unimib.it/cortesias3.html";

    private static final int ERROR_GENERIC = -1;
    public static final int ERROR_S3_NOT_AVAILABLE = -2;
    public static final int ERROR_WRONG_PASSWORD = -3;
    public static final int ERROR_CONNECTION_TIMEOUT = -4;
    public static final int ERROR_FACULTY_TO_CHOOSE = -5;
    private static final int ERROR_CAREER_OVER = -6;

    public static final int OK_LOGGED_IN = 1;
    private static final int OK_LOGGED_OUT = 2;


    private S3Helper() {}


    private static URL buildUrl(User user, String url) {
        String url_string = url +
                "JSESSIONID=" + user.getSessionID();
        if (user.isFacultyChosen())
                url_string += "?stu_id=" + user.getSelectedFaculty();
        URL result;

        try {
            result = new URL(url_string);
        } catch (MalformedURLException e) {
            result = null;
        }
        return result;
    }

    static SSLSocketFactory getSocketFactory(Context context) {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getResources().openRawResource(R.raw.terenasslca3);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.e("CipherUsed", session.getCipherSuite());
                    return hostname.compareTo("s3w.si.unimib.it") == 0;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            return sslContext.getSocketFactory();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return  null;
    }

    private static HttpsURLConnection secureConnection(HttpsURLConnection con, Context context) {
        // Tell the URLConnection to use a SocketFactory from our SSLContext
        con.setSSLSocketFactory(getSocketFactory(context));

        return con;
    }

    static HttpsURLConnection getPage(User user, String url, HashMap<String, String> headers,
                                             Context context) throws IOException {

        String USER_AGENT = System.getProperty("http.agent");

        HttpsURLConnection con = (HttpsURLConnection) buildUrl(user, url).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Host", "s3w.si.unimib.it");
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Upgrade-Insecure-Request", "1");
        if (user.getAuthToken() != null)
            con.setRequestProperty("Authorization", "Basic " + user.getAuthToken());
        if (user.getSessionID() != null)
            con.setRequestProperty("Cookie", "JSESSIONID=" + user.getSessionID());

        if (headers != null) for (String key : headers.keySet())
            con.setRequestProperty(key, headers.get(key));
        con.setConnectTimeout(5000);

        con = secureConnection(con, context);

        String cookie = con.getHeaderField("Set-Cookie");
        if (cookie != null && !cookie.isEmpty()) {
            user = UserUtils.updateSessionId(user, cookie, context);
            con = getPage(user, url, headers, context);
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



    private static int doLogin(User user, Context context) throws SocketTimeoutException {
        int ris;

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html");
        try {
            int respCode;
            HttpsURLConnection resp = null;

            if (!user.isFake()) {
                // Try to contact the server
                resp = getPage(user, URL_LIBRETTO, headers, context);
                respCode = resp.getResponseCode();
            } else {
                respCode = HttpURLConnection.HTTP_OK;
            }

            // If the server is not available
            if (respCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                ris = ERROR_S3_NOT_AVAILABLE;
                Log.e("LOGIN_PROCESS", "S3 not available.");
            }
            else {
                Log.d("LOGIN_PROCESS", "S3 available.");
                String html = null;

                if (!user.isFake()) {
                    // If the server gives me a new JSESSIONID cookie value
                    if (resp != null && resp.getHeaderField("Set-Cookie") != null) {
                        // Save it
                        String jsessionid = resp.getHeaderField("Set-Cookie");
                        user = UserUtils.updateSessionId(user, jsessionid, context);

                        Log.d("LOGIN_PROCESS", "Got new JSESSIONID");
                    }
                    Log.d("LOGIN_PROCESS", "SESSION ID = " + user.getSessionID());
                    Log.d("LOGIN_PROCESS", "AUTH TOKEN = " + user.getAuthToken());
                }

                // If the user needs to authenticate
                if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("LOGIN_PROCESS", "User not authenticated yet.");
                    headers = new HashMap<>();
                    headers.put("Accept", "text/html");

                    // Do login
                    resp = getPage(user, URL_LOGIN, headers, context);
                    respCode = resp.getResponseCode();
                    try {
                        html = getHTML(resp.getInputStream());
                    } catch (IOException e) {
                        html = null;
                    }
                }

                // If the server recognise the user as logged-in
                if (respCode == HttpURLConnection.HTTP_OK) {
                    Log.d("LOGIN_PROCESS", "User logged in.");

                    // If I'm sure the user only has one faculty
                    if (!user.isFirstLogin() && user.hasOneFaculty()) {
                        if (!isCareerOver(user, context)) {
                            Log.d("LOGIN_PROCESS", "FINISHED - OK.");
                            ris = OK_LOGGED_IN;
                        } else {
                            Log.d("LOGIN_PROCESS", "FINISHED - CAREER OVER.");
                            ris = ERROR_CAREER_OVER;
                        }
                    }
                    // If the user may have more faculties
                    else {
                        Log.d("LOGIN_PROCESS", "User might have multiple faculties.");

                        // If the app has the list of faculty to choose
                        // but the user hasn't chosen one yet
                        if (user.shouldChooseFaculty()) {
                            Log.d("LOGIN_PROCESS", "User needs to choose the faculty.");
                            ris = ERROR_FACULTY_TO_CHOOSE;
                        }

                        // If the user has chosen the faculty
                        else if (user.isFacultyChosen()) {
                            Log.d("LOGIN_PROCESS", "User has chosen the faculty.");
                            ris = handleFacultyChoice(user, context);
                        }

                        // If the app doesn't know if the user has multiple faculties,
                        // hence the app doesn't have the list of faculties
                        else {
                            SparseArray<String> faculties = hasMultiFaculty(user, html, context);

                            // If the user has only one faculty
                            if (faculties == null) {
                                Log.d("LOGIN_PROCESS", "ONLY ONE FACULTY.");

                                ris = OK_LOGGED_IN;
                            }

                            // If the user has multiple faculties
                            else {
                                Log.d("LOGIN_PROCESS", "Multiple faculties found.");
                                for (int i=0; i<faculties.size(); i++)
                                    Log.d("LOGIN_PROCESS",
                                            "Faculty: " + faculties.valueAt(i));

                                user.setFaculties(faculties);
                                UserUtils.saveUser(user, context);

                                ris = ERROR_FACULTY_TO_CHOOSE;
                            }
                        }
                    }
                }
                // Otherwise the the password is not valid
                else {
                    Log.d("LOGIN_PROCESS", "WRONG PASSWORD.");
                    ris = ERROR_WRONG_PASSWORD;
                }

            }
        } catch (SocketTimeoutException e){
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            ris = ERROR_GENERIC;
        }

        return ris;
    }

    private static boolean isCareerOver(User user, Context context) throws IOException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
            NoSuchProviderException {
        boolean ris = false;
        if (user.isFake()) return false;

        try {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Accept", "text/html");

            HttpURLConnection response = S3Helper.getPage(user, S3Helper.URL_HOME, headers, context);
            String result = S3Helper.getHTML(response.getInputStream());

            Document doc = Jsoup.parse(result);
            Element el = doc.getElementById("gu-textStatusStudente");

            if (el.text().toLowerCase().contains("cessato"))
                ris = true;
        } catch (IOException e){
            Utils.saveBugReport(e);
            throw e;
        }

        return ris;
    }



    private static void downloadUserData(User user, Context context) throws IOException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
            NoSuchProviderException{

        if (user.isFake()) {
            Utils.user.setMatricola("293640");
            Utils.user.setName("Pippo Pluto");
            Utils.user.setTotalCFU(42);
            Utils.user.setAverageMark(24);

            UserUtils.saveUser(Utils.user, context);
            return;
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html");

        try {
            HttpURLConnection response = S3Helper.getPage(user, S3Helper.URL_LIBRETTO, headers, context);
            Document doc;
            String result = S3Helper.getHTML(response.getInputStream());

            if (response.getResponseCode() == HttpURLConnection.HTTP_OK){
                doc = Jsoup.parse(result);
                Element el2 = doc.select("#esse3old > table:has(div.titolopagina)").first();

                if (el2 != null) {
                    String matricola = el2.text();
                    matricola = matricola.substring(matricola.indexOf("MAT. ") + 5);
                    matricola = matricola.substring(0, 6);

                    Utils.user.setMatricola(matricola);
                    Log.d("LOGIN_USER_DATA", "Matricola: " + matricola);
                }
            }

            response = S3Helper.getPage(user, S3Helper.URL_HOME, headers, context);
            result = S3Helper.getHTML(response.getInputStream());
            doc = Jsoup.parse(result);
            Element el = doc.select("div#sottotitolo-menu-principale").first();

            String name = el.text();
            Utils.user.setName(name);
            Log.d("LOGIN_USER_DATA", "Name: " + name);

            Elements els = doc.select("div#gu-boxRiepilogoEsami dl.record-riga dd");
            String averageTmp = els.get(2).text();
            averageTmp = averageTmp.substring(0, averageTmp.length()-3);
            String cfuTmp = els.get(3).text();
            cfuTmp = cfuTmp.substring(0, cfuTmp.indexOf("/"));
            float averageMark = Float.parseFloat(averageTmp);
            int totalCfu = Integer.parseInt(cfuTmp);
            Utils.user.setTotalCFU(totalCfu);
            Utils.user.setAverageMark(averageMark);

            Log.d("LOGIN_USER_DATA", "Average mark: " + averageTmp);
            Log.d("LOGIN_USER_DATA", "CFU: " + cfuTmp);

            UserUtils.saveUser(Utils.user, context);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e){
            Utils.saveBugReport(e);

            throw e;
        }
    }

    private static SparseArray<String> hasMultiFaculty(User user, String html, Context context)
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, NoSuchProviderException {
        if (user.isFake()) return downloadFacultiesList(user, context);

        if (html == null) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Accept", "text/html");

            HttpsURLConnection response = S3Helper.getPage(user, S3Helper.URL_HOME, headers, context);
            html = S3Helper.getHTML(response.getInputStream());
        }

        Document doc = Jsoup.parse(html);
        Element el1 = doc.select("#titolo-menu-principale").first();

        boolean hasMultiFaculty = el1.text().toLowerCase().equals("registrato");

        if (hasMultiFaculty)
            return downloadFacultiesList(user, context);
        else
            return null;
    }

    private static SparseArray<String> downloadFacultiesList(User user, Context context) throws IOException,
            CertificateException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, NoSuchProviderException {
        SparseArray<String> courses = new SparseArray<>();

        if (user.isFake()) {
            courses.put(34829, "Faculty of IT");
            courses.put(19347, "Faculty of Science");
            courses.put(58240, "Faculty of Chemistry");

            return courses;
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html");

        try{
            HttpURLConnection response = S3Helper.getPage(user, S3Helper.URL_LIBRETTO,
                    headers, context);
            String result = S3Helper.getHTML(response.getInputStream());

            Document doc = Jsoup.parse(result);
            Elements els = doc.select("table#gu_table_sceltacarriera tbody tr");

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
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e){
            Utils.saveBugReport(e);
            throw e;
        }

        return courses;
    }

    private static int handleFacultyChoice(User user, Context context) {
        int ris;

        // If the user has only one faculty then is automatically logged-in
        if (user.hasOneFaculty())
            ris = OK_LOGGED_IN;
            // Otherwise tell the server which faculty the user wants
        else {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Accept", "text/html");

            try {
                if (user.isFake()) {
                    downloadUserData(user, context);

                    return OK_LOGGED_IN;
                }

                // Load the chosen faculty
                String faculty = user.getSelectedFacultyUrl();

                // If there is one
                if (!TextUtils.isEmpty(faculty)) {
                    // Tell the server which faculty the user wants
                    int code = getPage(user, faculty, headers, context).getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        if (!isCareerOver(user, context)) {
                            ris = OK_LOGGED_IN;

                            Log.d("LOGIN", "Downloading user data");
                            downloadUserData(user, context);
                        }
                        else
                            ris = ERROR_CAREER_OVER;
                    }
                    else {
                        getPage(user, URL_LOGIN, headers, context).getResponseCode();
                        code = getPage(user, faculty, headers, context).getResponseCode();

                        if (code == HttpURLConnection.HTTP_OK) {
                            if (!isCareerOver(user, context)) {
                                ris = OK_LOGGED_IN;

                                Log.d("LOGIN", "Downloading user data");
                                downloadUserData(user, context);
                            }
                            else
                                ris = ERROR_CAREER_OVER;
                        }
                        else
                            ris = ERROR_GENERIC;
                    }
                }
                else
                    ris = ERROR_FACULTY_TO_CHOOSE;
            } catch (Exception e) {
                e.printStackTrace();
                ris = ERROR_GENERIC;
            }
        }

        return ris;
    }



    public static class LoginLoader extends AsyncTaskLoader<User> {

        // Weak references will still allow the Context to be garbage-collected
        private final WeakReference<Activity> mActivity;

        private final String mUsername;
        private final String mPassword;
        private final boolean mFake;

        public LoginLoader(Activity activity, User user) {
            super(activity);

            this.mActivity = new WeakReference<>(activity);
            this.mUsername = user.getUsername();
            this.mPassword = user.getPassword();
            this.mFake = user.isFake();
        }

        @Override
        public User loadInBackground() {
            Log.d("LOADER_LOGIN", "STARTED");

            int loggedIn;
            Context context = mActivity.get();
            if (context == null) return null;

            User user = UserUtils.getUser(context);
            if (user == null) {
                user = new User(mUsername, mPassword);
                user.setFake(mFake);
            }

            try {
                Log.d("LOGIN", "Trying to login");
                loggedIn = doLogin(user, context);

                if (loggedIn == OK_LOGGED_IN) {
                    Log.d("LOGIN", "RESULT: LOGIN COMPLETED. (" + loggedIn + ")");

                    Log.d("LOGIN", "Downloading user data");
                    downloadUserData(user, context);

                    user.setIsFirstLogin(false);

                    UserUtils.saveUser(user, context);
                }
            } catch (SocketTimeoutException e){
                loggedIn = ERROR_CONNECTION_TIMEOUT;
            } catch (Exception e) {
                loggedIn = ERROR_GENERIC;
                Utils.saveBugReport(e);
            }

            user.setTag(loggedIn);

            return user;
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }
    }

    public static class ConfirmFacultyLoader extends  AsyncTaskLoader<User> {

        // Weak references will still allow the Context to be garbage-collected
        private final WeakReference<Activity> mActivity;
        private final User mUser;

        public ConfirmFacultyLoader(Activity activity, User user) {
            super(activity);

            this.mActivity = new WeakReference<>(activity);

            if (user == null) user = UserUtils.getUser(activity);
            this.mUser = user;
        }


        @Override
        public User loadInBackground() {
            int ris;
            Context context = mActivity.get();

            try {
                ris = doLogin(mUser, context);
            } catch (SocketTimeoutException e){
                ris = ERROR_CONNECTION_TIMEOUT;
            } catch (Exception e) {
                ris = ERROR_GENERIC;
                Utils.saveBugReport(e);
            }
            mUser.setTag(ris);

            return mUser;
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
            Context context = mActivity.get();
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Accept", "text/html");

            try {
                getPage(mUser, URL_LOGOUT, headers, context);
                boolean loggedOut = UserUtils.removeUser(context);

                if (loggedOut)
                    mUser.setTag(OK_LOGGED_OUT);
                else
                    return null;
            } catch (Exception e) {
                return null;
            }

            return mUser;
        }
    }
    
}
