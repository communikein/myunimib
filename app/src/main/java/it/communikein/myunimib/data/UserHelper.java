package it.communikein.myunimib.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.utilities.Utils;

@Singleton
public class UserHelper {

    private static final String TAG = UserHelper.class.getSimpleName();

    private final String ACCOUNT_TYPE;

    private final AccountManager mAccountManager;
    private final MutableLiveData<User> mUser;

    public interface AccountRemovedListener {
        void onAccountRemoved(boolean removed);
    }

    public interface AccountRemoveErrorListener {
        void onAccountRemoveError(String error);
    }


    @Inject
    public UserHelper(Context context) {
        this.mAccountManager = AccountManager.get(context.getApplicationContext());
        this.ACCOUNT_TYPE = context.getString(R.string.account_type);
        this.mUser = new MutableLiveData<>();

        initUser();
    }

    private void initUser() {
        mUser.postValue(getUser());
    }

    public void updateSessionId(String sessionID) {
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        Account selected;

        if (accounts.length > 0) {
            selected = accounts[0];

            mAccountManager.setUserData(selected,
                    UserAuthentication.PREF_SESSION_ID, sessionID);
        }
    }

    public void updateChosenFaculty(int chosenFaculty) {
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        Account selected;

        if (accounts.length > 0) {
            selected = accounts[0];

            mAccountManager.setUserData(selected,
                    UserAuthentication.PREF_SELECTED_FACULTY, String.valueOf(chosenFaculty));
        }
    }

    public void saveUserAuth(UserAuthentication user){
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        Account selected;

        if (accounts.length > 0) {
            selected = accounts[0];

            mAccountManager.setUserData(selected,
                    UserAuthentication.PREF_SESSION_ID, user.getSessionID());
            mAccountManager.setUserData(selected,
                    UserAuthentication.PREF_FACULTIES, user.getFacultiesJSON().toString());
            mAccountManager.setUserData(selected,
                    UserAuthentication.PREF_SELECTED_FACULTY, String.valueOf(user.getSelectedFaculty()));
        }
    }

    public void saveUser(User user){
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        Account selected;

        if (accounts.length > 0) {
            selected = accounts[0];

            mAccountManager.setUserData(selected,
                    User.PREF_SESSION_ID, user.getSessionID());
            mAccountManager.setUserData(selected,
                    User.PREF_FACULTIES, user.getFacultiesJSON().toString());
            mAccountManager.setUserData(selected,
                    User.PREF_SELECTED_FACULTY, String.valueOf(user.getSelectedFaculty()));

            mAccountManager.setUserData(selected,
                    User.PREF_NAME, user.getName());
            mAccountManager.setUserData(selected,
                    User.PREF_MATRICOLA, user.getMatricola());
            mAccountManager.setUserData(selected,
                    User.PREF_TOTAL_CFU, String.valueOf(user.getTotalCFU()));
            mAccountManager.setUserData(selected,
                    User.PREF_AVERAGE_MARK, String.valueOf(user.getAverageMark()));
            mAccountManager.setUserData(selected,
                    User.PREF_FAKE, String.valueOf(user.isFake()));
        }
    }

    public User getUser(){
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        Account selected;

        if (accounts.length > 0) {
            selected = accounts[0];

            String password = mAccountManager.getPassword(selected);
            String username = selected.name;
            String sessionID = mAccountManager.getUserData(selected, User.PREF_SESSION_ID);

            String name = mAccountManager.getUserData(selected, User.PREF_NAME);
            String matricola = mAccountManager.getUserData(selected, User.PREF_MATRICOLA);

            String cfu = mAccountManager.getUserData(selected, User.PREF_TOTAL_CFU);
            int totalCFU = TextUtils.isEmpty(cfu) ? User.ERROR_TOTAL_CFU : Integer.parseInt(cfu);
            String score = mAccountManager.getUserData(selected, User.PREF_AVERAGE_MARK);
            float averageScore = TextUtils.isEmpty(score) ? User.ERROR_AVERAGE_MARK : Float.parseFloat(score);

            String fake = mAccountManager.getUserData(selected, User.PREF_FAKE);
            boolean isFake = !TextUtils.isEmpty(fake) && Boolean.parseBoolean(fake);

            User user = new User(username, password, name, averageScore, totalCFU, matricola);
            user.setSessionID(sessionID);
            user.setFake(isFake);

            String faculties_tmp = mAccountManager.getUserData(selected, User.PREF_FACULTIES);
            if (!TextUtils.isEmpty(faculties_tmp)) {
                try {
                    JSONObject faculties_json = new JSONObject(faculties_tmp);
                    user.setFaculties(faculties_json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String selected_faculty = mAccountManager.getUserData(selected, User.PREF_SELECTED_FACULTY);
            int selectedFaculty = TextUtils.isEmpty(selected_faculty) ? -1 : Integer.parseInt(selected_faculty);
            user.setSelectedFaculty(selectedFaculty);

            return user;
        }

        return null;
    }

    public LiveData<User> getObservableUser() {
        return mUser;
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
                                Utils.saveBugReport(e, TAG);
                                errorListener.onAccountRemoveError(null);
                            } catch (OperationCanceledException e) {
                                Utils.saveBugReport(e, TAG);

                                String error = activity.getString(R.string.error_logout_cancelled);
                                errorListener.onAccountRemoveError(error);
                            } catch (AuthenticatorException e) {
                                Utils.saveBugReport(e, TAG);
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
                        Utils.saveBugReport(e, TAG);
                        errorListener.onAccountRemoveError(null);
                    } catch (OperationCanceledException e) {
                        Utils.saveBugReport(e, TAG);

                        String error = activity.getString(R.string.error_logout_cancelled);
                        errorListener.onAccountRemoveError(error);
                    } catch (AuthenticatorException e) {
                        Utils.saveBugReport(e, TAG);
                        errorListener.onAccountRemoveError(null);
                    }
                }, null);
            }
        }
    }

}
