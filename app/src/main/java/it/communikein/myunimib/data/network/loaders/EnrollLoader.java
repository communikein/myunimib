package it.communikein.myunimib.data.network.loaders;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import it.communikein.myunimib.data.UserHelper;
import it.communikein.myunimib.data.model.Exam;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.utilities.Utils;

public class EnrollLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = EnrollLoader.class.getSimpleName();

    public static final int STATUS_ERROR_QUESTIONNAIRE_TO_FILL = -1;
    public static final int STATUS_ERROR_CERTIFICATE = -2;
    public static final int STATUS_ERROR_GENERAL = -3;

    public static final int STATUS_STARTED = 1;
    public static final int STATUS_ENROLLMENT_OK = 2;
    public static final int STATUS_CERTIFICATE_DOWNLOADED = 3;

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;
    private final Exam mExam;

    private EnrollUpdatesListener mEnrollUpdatesListener;
    public interface EnrollUpdatesListener {
        void onEnrollmentUpdate(int status);
    }

    private EnrollCompleteListener mEnrollCompleteListener;
    public interface EnrollCompleteListener {
        void onEnrollmentCompleted();
    }

    EnrollLoader(Activity activity, Exam exam) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mExam = exam;
    }

    public EnrollLoader setEnrollUpdatesListener(EnrollUpdatesListener listener) {
        this.mEnrollUpdatesListener = listener;

        return this;
    }

    public EnrollLoader setEnrollCompleteListener(EnrollCompleteListener listener) {
        this.mEnrollCompleteListener = listener;

        return this;
    }

    @Override
    public Boolean loadInBackground() {
        Activity context = mActivity.get();
        if (context == null) return null;

        String urlParameters = mExam.examIdToUrl();

        try {
            mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_STARTED);

            UserHelper userHelper = new UserHelper(context);
            User user = userHelper.getUser();

            Bundle result = UnimibNetworkDataSource
                    .authenticatedGET(S3Helper.URL_ENROLL_TO, user,
                            urlParameters, true,
                            context, userHelper::updateSessionId);
            int s3_response = result.getInt(UnimibNetworkDataSource.PARAM_KEY_RESPONSE);
            String html = result.getString(UnimibNetworkDataSource.PARAM_KEY_HTML);

            if (s3_response == -1 || html == null) {
                return false;
            }
            else {
                Document doc = Jsoup.parse(html);
                Element element = doc.select("#app-text_esito_pren_msg").first();
                String responseText = element.text().toLowerCase();

                if (!TextUtils.isEmpty(responseText) &&
                        responseText.contains("non risulta compilato il questionario")) {
                    mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_ERROR_QUESTIONNAIRE_TO_FILL);
                } else if (responseText.equals("") && s3_response == HttpURLConnection.HTTP_OK) {
                    mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_ENROLLMENT_OK);
                    mEnrollCompleteListener.onEnrollmentCompleted();

                    boolean downloaded = S3Helper.downloadCertificate(user, mExam,
                            context, userHelper::updateSessionId);
                    if (downloaded) {
                        boolean certFound = mExam.getCertificatePath().exists();
                        if (certFound)
                            mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_CERTIFICATE_DOWNLOADED);
                    } else
                        mEnrollUpdatesListener.onEnrollmentUpdate(STATUS_ERROR_CERTIFICATE);

                    return true;
                }
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
