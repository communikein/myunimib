package it.communikein.myunimib.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Faculty;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.utilities.Utils;

@Singleton
public class UserHelper {

    private static final String TAG = UserHelper.class.getSimpleName();

    private final String ACCOUNT_TYPE;

    private final SharedPreferences mSharedPreferences;
    private final AccountManager mAccountManager;

    public interface AccountRemovedListener {
        void onAccountRemoved(boolean removed);
    }

    public interface AccountRemoveErrorListener {
        void onAccountRemoveError(String error);
    }


    @Inject
    public UserHelper(Context context) {
        this.mSharedPreferences = context.getSharedPreferences("myunimib", Context.MODE_PRIVATE);
        this.mAccountManager = AccountManager.get(context.getApplicationContext());
        this.ACCOUNT_TYPE = context.getString(R.string.account_type);
    }


    public void updateSessionId(String sessionID) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        editor.putString(User.PREF_SESSION_ID, sessionID);

        editor.apply();
    }

    public void updateChosenFaculty(Faculty chosenFaculty) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        if (chosenFaculty != null)
            editor.putString(User.PREF_SELECTED_FACULTY, chosenFaculty.toJson());

        editor.apply();
    }

    public void saveUser(User user){
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        editor.putString(User.PREF_SESSION_ID, user.getSessionId());
        if (user.getSelectedFaculty() != null)
            editor.putString(User.PREF_SELECTED_FACULTY, user.getSelectedFaculty().toJson());

        editor.apply();
    }

    public User getUser(){
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        Account selected;

        if (accounts.length > 0) {
            selected = accounts[0];

            String password = mAccountManager.getPassword(selected);
            String username = selected.name;

            String sessionID = mSharedPreferences.getString(User.PREF_SESSION_ID, "");
            String selected_faculty = mSharedPreferences.getString(User.PREF_SELECTED_FACULTY, "");

            User user = new User(username, password);
            user.setSessionId(sessionID);

            if (!TextUtils.isEmpty(selected_faculty))
                user.setSelectedFaculty(new Faculty(selected_faculty));
            else
                user.setSelectedFaculty(null);

            return user;
        }

        return null;
    }


    public void deleteUser(Activity activity, AccountRemovedListener listener,
                           AccountRemoveErrorListener errorListener) {
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        Account selected;

        if (accounts.length > 0) {
            selected = accounts[0];

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                /*
                 * Trying to call this on an older Android version results in a
                 * NoSuchMethodError exception. There is no AppCompat version of the
                 * AccountManager API to avoid the need for this version check at runtime.
                 */
                mAccountManager.removeAccount(selected, activity,
                        accountManagerFuture -> {
                            try {
                                Bundle data = accountManagerFuture.getResult();
                                boolean isRemoved = data.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);

                                listener.onAccountRemoved(isRemoved);
                            } catch (IOException e) {
                                Utils.saveBugReport(e, TAG, "UserHelper.deleteUser");
                                errorListener.onAccountRemoveError(null);
                            } catch (OperationCanceledException e) {
                                Utils.saveBugReport(e, TAG, "UserHelper.deleteUser");

                                String error = activity.getString(R.string.error_logout_cancelled);
                                errorListener.onAccountRemoveError(error);
                            } catch (AuthenticatorException e) {
                                Utils.saveBugReport(e, TAG, "UserHelper.deleteUser");
                                errorListener.onAccountRemoveError(null);
                            }
                        }, null);
            } else {
                /* Note that this needs the MANAGE_ACCOUNT permission on SDK <= 22. */
                mAccountManager.removeAccount(selected, accountManagerFuture -> {
                    try {
                        boolean isRemoved = accountManagerFuture.getResult();

                        listener.onAccountRemoved(isRemoved);
                    } catch (IOException e) {
                        Utils.saveBugReport(e, TAG, "UserHelper.deleteUser");
                        errorListener.onAccountRemoveError(null);
                    } catch (OperationCanceledException e) {
                        Utils.saveBugReport(e, TAG, "UserHelper.deleteUser");

                        String error = activity.getString(R.string.error_logout_cancelled);
                        errorListener.onAccountRemoveError(error);
                    } catch (AuthenticatorException e) {
                        Utils.saveBugReport(e, TAG, "UserHelper.deleteUser");
                        errorListener.onAccountRemoveError(null);
                    }
                }, null);
            }
        }
    }

}
