package it.communikein.myunimib.data.network.loaders;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

import it.communikein.myunimib.data.UserHelper;
import it.communikein.myunimib.data.model.Exam;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.utilities.Utils;

public class UnEnrollLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = UnEnrollLoader.class.getSimpleName();

    public static final String URL_UNENROLL_FROM =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/CancellaAppello.do;";
    private static final String URL_CONFIRM_UNENROLL =
            "https://s3w.si.unimib.it/esse3/auth/studente/Appelli/ConfermaCancellaAppello.do;";

    public static final int STATUS_ERROR_GENERAL = -3;

    public static final int STATUS_STARTED = 1;
    public static final int STATUS_UNENROLLMENT_OK = 2;


    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;
    private final Exam mExam;

    private UnEnrollUpdatesListener mUnEnrollUpdatesListener;
    public interface UnEnrollUpdatesListener {
        void onUnEnrollmentUpdate(int status);
    }

    private UnEnrollCompleteListener mUnEnrollCompleteListener;
    public interface UnEnrollCompleteListener {
        void onUnEnrollmentCompleted();
    }

    UnEnrollLoader(Activity activity, Exam exam) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mExam = exam;
    }

    public UnEnrollLoader setUnEnrollUpdatesListener(UnEnrollUpdatesListener listener) {
        this.mUnEnrollUpdatesListener = listener;

        return this;
    }

    public UnEnrollLoader setUnEnrollCompleteListener(UnEnrollCompleteListener listener) {
        this.mUnEnrollCompleteListener = listener;

        return this;
    }


    @Nullable
    @Override
    public Boolean loadInBackground() {
        Activity context = mActivity.get();
        if (context == null) return null;

        String urlParameters = mExam.examIdToUrl();
        try {
            mUnEnrollUpdatesListener.onUnEnrollmentUpdate(STATUS_STARTED);

            UserHelper userHelper = new UserHelper(context);
            User user = userHelper.getUser();

            // TODO: write the logic to un-enroll the exam

            Bundle result = new Bundle();
            int s3_response = result.getInt(UnimibNetworkDataSource.PARAM_KEY_RESPONSE, -1);
            String html = result.getString(UnimibNetworkDataSource.PARAM_KEY_HTML, null);

            if (s3_response == -1 || html == null) {
                return false;
            }
            else {
                Document doc = Jsoup.parse(html);

                // TODO: write the logic to parse the result of the action taken

                if (s3_response == HttpURLConnection.HTTP_OK) {
                    mUnEnrollUpdatesListener.onUnEnrollmentUpdate(STATUS_UNENROLLMENT_OK);
                    mUnEnrollCompleteListener.onUnEnrollmentCompleted();
                    return true;
                }
            }
        } catch (Exception e) {
            mUnEnrollUpdatesListener.onUnEnrollmentUpdate(STATUS_ERROR_GENERAL);
            Utils.saveBugReport(e, TAG);
        }

        return false;
    }
}
