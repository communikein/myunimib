package it.communikein.myunimib.data.network.loaders;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import it.communikein.myunimib.accountmanager.AccountUtils;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.UserHelper;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.utilities.Utils;

import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_CAREER_OVER;
import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_CONNECTION_TIMEOUT;
import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_FACULTY_TO_CHOOSE;
import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_GENERIC;
import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_RESPONSE_NULL;
import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_S3_NOT_AVAILABLE;
import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_WRONG_PASSWORD;
import static it.communikein.myunimib.data.network.loaders.S3Helper.OK_LOGGED_IN;
import static it.communikein.myunimib.data.network.loaders.S3Helper.URL_LIBRETTO;

public class LoginLoader extends AsyncTaskLoader<User> {

    private static final String TAG = LoginLoader.class.getSimpleName();

    private static final String RESULT = "RESULT";

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;

    private User mUser;

    LoginLoader(Activity activity, UserAuthentication user) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mUser = (User) user;
    }

    @Override
    public User loadInBackground() {
        Log.d(TAG, "STARTED");

        /* Get the context from the activity, if null end the login process */
        Context context = mActivity.get();
        if (context == null) return null;

        UserHelper userHelper = new UserHelper(context);
        int loggedIn;
        try {
            /* Try logging in the user */
            Bundle result = doLogin(mUser, context, userHelper);
            loggedIn = result.getInt(RESULT);

            String sessionId = result.getString(User.PREF_SESSION_ID);
            String facultiesString = result.getString(User.PREF_FACULTIES);

            if (sessionId != null)
                mUser.setSessionID(sessionId);
            if (facultiesString != null) {
                JSONObject facultiesJson = new JSONObject(facultiesString);

                mUser.setFaculties(facultiesJson);
            }

            if (loggedIn == ERROR_FACULTY_TO_CHOOSE) {
                createAccount(mUser, context);
            }

            /*
             * If the server recognise the user as logged in, download the user's data and
             * notify the user that the login process is completed successfully.
             */
            if (loggedIn == OK_LOGGED_IN) {
                Log.d(TAG, "RESULT: LOGIN COMPLETED. (" + loggedIn + ")");

                Log.d(TAG, "Downloading user data");
                mUser = S3Helper.downloadUserData(mUser, context, userHelper::updateSessionId);

                createAccount(mUser, context);
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

    private Bundle doLogin(User user, Context context, UserHelper userHelper) throws SocketTimeoutException {
        Bundle result = new Bundle();
        int ris;

        try {
            int respCode;
            HttpsURLConnection resp = null;

            /* If this is a real login, try to contact the server and save its response */
            if (!user.isFake()) {
                resp = S3Helper.getPage(user, URL_LIBRETTO, context, userHelper::updateSessionId);
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
                        //user = UserHelper.updateSessionId(user, jsessionid, context);

                        Log.d(TAG, "Got new JSESSIONID");

                        /* Try to get the page with the new session ID */
                        resp = S3Helper.getPage(user, URL_LIBRETTO, context, userHelper::updateSessionId);
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
                            String html = S3Helper.getHTML(resp.getInputStream());
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
                        if (!S3Helper.isCareerOver(user, document)) {
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
                            SparseArray<String> faculties = S3Helper.hasMultiFaculty(user, document);

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

    private void createAccount(User user, Context context) {
        /* Save the account on the device via Account Manager */
        AccountManager accountManager = AccountManager.get(context.getApplicationContext());
        /* Create a new UNIMIB account to save on the device */
        Account account = new Account(user.getUsername(), AccountUtils.ACCOUNT_TYPE);

        if (accountManager.getPassword(account) == null)
            accountManager.addAccountExplicitly(account, user.getPassword(), null);

        new UserHelper(context).saveUser(user);
    }

}