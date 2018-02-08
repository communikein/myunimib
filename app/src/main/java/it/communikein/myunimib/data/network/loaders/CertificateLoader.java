package it.communikein.myunimib.data.network.loaders;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import it.communikein.myunimib.data.UserHelper;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.utilities.Utils;

import it.communikein.myunimib.data.network.loaders.S3Helper.NewSessionIdListener;

public class CertificateLoader extends AsyncTaskLoader<Cursor> {

    private static final String TAG = CertificateLoader.class.getSimpleName();

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;
    private final EnrolledExam mExam;

    CertificateLoader(Activity activity, EnrolledExam exam) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mExam = exam;
    }

    @Override
    public Cursor loadInBackground() {
        Activity context = mActivity.get();
        if (context == null) return null;

        try {
            UserHelper userHelper = new UserHelper(context);
            User user = userHelper.getUser();

            S3Helper.downloadCertificate(user, mExam, context, userHelper::updateSessionId);
            return null;
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            Utils.saveBugReport(e, TAG);
        }

        return null;
    }
}
