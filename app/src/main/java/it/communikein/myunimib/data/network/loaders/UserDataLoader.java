package it.communikein.myunimib.data.network.loaders;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import it.communikein.myunimib.data.UserHelper;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.utilities.Utils;

import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_CONNECTION_TIMEOUT;
import static it.communikein.myunimib.data.network.loaders.S3Helper.ERROR_GENERIC;
import static it.communikein.myunimib.data.network.loaders.S3Helper.OK_UPDATED;
import static it.communikein.myunimib.data.network.loaders.S3Helper.downloadUserData;

public class UserDataLoader extends AsyncTaskLoader<User> {

    private static final String TAG = UserDataLoader.class.getSimpleName();

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;

    UserDataLoader(Activity activity) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
    }

    @Override
    public User loadInBackground() {
        Log.d(TAG, "STARTED");

        /* Get the context from the activity, if null end the login process */
        Context context = mActivity.get();
        if (context == null) return null;

        UserHelper userHelper = new UserHelper(context);
        User user = userHelper.getUser();

        int loggedIn;
        try {
            Log.d(TAG, "Downloading user data");

            user = S3Helper.downloadUserData(user, context, userHelper::updateSessionId);
            userHelper.saveUser(user);

            loggedIn = OK_UPDATED;
        } catch (SocketTimeoutException e){
            loggedIn = ERROR_CONNECTION_TIMEOUT;
        } catch (IOException e) {
            loggedIn = ERROR_GENERIC;
            Utils.saveBugReport(e, TAG);
        }

        user.setTag(loggedIn);

        return user;
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}