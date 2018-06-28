package it.communikein.myunimib.ui.login;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.Snackbar;


import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import dagger.android.AndroidInjection;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.UserHelper;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.loaders.S3Helper;
import it.communikein.myunimib.databinding.ActivityLoginBinding;
import it.communikein.myunimib.ui.AuthAppCompatActivity;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.utilities.NetworkHelper;
import it.communikein.myunimib.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.viewmodel.LoginViewModel;
import it.communikein.myunimib.viewmodel.factory.LoginViewModelFactory;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends AuthAppCompatActivity implements
        LoaderManager.LoaderCallbacks, EasyPermissions.PermissionCallbacks,
        FacultyFragment.FacultyChooseProcessCallback,
        LoginFragment.LoginProcessCallback,
        PersonalDataFragment.PersonalDataCallback {

    private ActivityLoginBinding mBinding;

    /* */
    @Inject
    LoginViewModelFactory viewModelFactory;

    /* */
    private LoginViewModel mViewModel;

    private AccountManager mAccountManager;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1001;
    private static final String ARG_USERNAME = "arg_username";
    private static final String ARG_PASSWORD = "arg_password";
    private static final String ARG_NAME = "arg_name";

    private static final int LOADER_LOGIN_ID = 2100;
    private static final int LOADER_FAKE_LOGIN_ID = 2101;
    private static final int LOADER_CONFIRM_FACULTY_ID = 2102;

    private ProgressDialog progress;

    private FragmentManager mFragmentManager;
    private LoginFragment mLoginFragment;
    private FacultyFragment mFacultyFragment;
    private PersonalDataFragment mPersonalDataFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(LoginViewModel .class);

        mFragmentManager = getSupportFragmentManager();
        initUI();
    }

    public LoginViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAccountManager == null) mAccountManager = AccountManager.get(this);

        // Check if the app has the permission to access the accounts saved on the device
        if (EasyPermissions.hasPermissions(this,
                android.Manifest.permission.GET_ACCOUNTS, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Check if there already is a registered ic_account on the device
            Account[] S3_accounts = mAccountManager.getAccountsByType(mViewModel.getAccountType());

            boolean isLoginVisible = mFragmentManager.findFragmentByTag(LoginFragment.TAG) != null;
            boolean isFacultyVisible = mFragmentManager.findFragmentByTag(FacultyFragment.TAG) != null;

            if (S3_accounts.length == 0) {
                if (!isLoginVisible) showLoginView();
                mViewModel.deleteUser();
            }
            else {
                User user = mViewModel.getUser();

                if (!user.isAuthTokenSet()) {
                    if (!isLoginVisible) showLoginView();
                }
                else if (user.hasMultiFaculty() && !user.isFacultyChosen()) {
                    if (!isFacultyVisible) showFacultyChoiceView();
                }
                else
                    startActivity(new Intent(this, MainActivity.class));
            }
        }
        else
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.error_permission_accounts_necessary),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    android.Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_fake_login:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mFragmentManager.popBackStackImmediate();
    }

    private void initUI() {
        mLoginFragment = LoginFragment.newInstance();
        mFacultyFragment = FacultyFragment.newInstance();
        mPersonalDataFragment = PersonalDataFragment.newInstance();

        showLoginView();

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.label_logging_in));
        progress.setCancelable(false);
    }

    private void showProgress(final boolean show) {
        if (show) progress.show();
        else if(progress != null && progress.isShowing()) progress.dismiss();
    }



    @SuppressWarnings("unchecked")
    private void handleLoginResults(final User user){
        showProgress(false);

        /* Get the login result */
        int ris = (int) user.getTag();
        switch (ris){

            /* If the user has to choose the faculty */
            case S3Helper.ERROR_FACULTY_TO_CHOOSE:

                /* Update the faculties list */
                getViewModel().setFaculties(user.getFaculties());

                /* Update the UI */
                showFacultyChoiceView();

                /* Signal the user */
                Snackbar.make(mBinding.loginView,
                        R.string.faculty_to_choose, Snackbar.LENGTH_LONG).show();
                break;

            /* If the login process is completed successfully */
            case S3Helper.OK_LOGGED_IN:
            case S3Helper.OK_UPDATED:
                /* Finalize login process */
                finishLogin(user);
                break;

            /* If S3 is not available */
            case S3Helper.ERROR_S3_NOT_AVAILABLE:
                /* Signal the user */
                Snackbar.make(mBinding.loginView, R.string.error_S3_not_available, Snackbar.LENGTH_LONG)
                        .show();
                break;

            /* If password or username are wrong */
            case S3Helper.ERROR_WRONG_PASSWORD:
                /* Show the credentials error */
                mLoginFragment.signalWrongCredentials();
                break;

            /* If the internet connection is too slow */
            case S3Helper.ERROR_CONNECTION_TIMEOUT:
                /* Signal the user */
                Snackbar.make(mBinding.loginView, R.string.error_connection_timeout, Snackbar.LENGTH_LONG)
                        .show();
                break;

            /* If any other error happens */
            default:
                /* Signal the user */
                Snackbar.make(mBinding.loginView, R.string.error_generic_exception, Snackbar.LENGTH_LONG)
                        .show();
                break;
        }
    }

    private void finishLogin(User user) {
        mViewModel.saveUser(user);
        showLoginView();
        showProgress(false);

        /* Since the user is logged in, start the Main Activity */
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void showFacultyChoiceView() {
        if (mFragmentManager.findFragmentByTag(FacultyFragment.TAG) != null)
            mFragmentManager.popBackStack();
        else {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left);
            fragmentTransaction.replace(R.id.container, mFacultyFragment, FacultyFragment.TAG);
            fragmentTransaction.addToBackStack(FacultyFragment.TAG);
            fragmentTransaction.commit();
        }
    }

    private void showLoginView() {
        if (mFragmentManager.findFragmentByTag(LoginFragment.TAG) != null) {
            mFragmentManager.popBackStack();
        }
        else {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left);
            fragmentTransaction.replace(R.id.container, mLoginFragment, LoginFragment.TAG);
            fragmentTransaction.addToBackStack(LoginFragment.TAG);
            fragmentTransaction.commit();
        }
    }

    private void showPersonalDataView() {
        if (mFragmentManager.findFragmentByTag(PersonalDataFragment.TAG) != null)
            mFragmentManager.popBackStack();
        else {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left);
            fragmentTransaction.replace(R.id.container, mPersonalDataFragment, PersonalDataFragment.TAG);
            fragmentTransaction.addToBackStack(PersonalDataFragment.TAG);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onFacultyChosen() {
        /* Show a progress dialog and start the Loader for completion. */
        showProgress(true);

        /* Do the login again */
        getSupportLoaderManager()
                .initLoader(LOADER_CONFIRM_FACULTY_ID, null, this)
                .forceLoad();
    }

    @Override
    public void onLoginProcessBlocked(int errorCode) {
        /* Tell the user that there is no internet available. */
        Snackbar.make(
                mBinding.loginView,
                R.string.error_no_internet,
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLoginProcessOk(String username, String password) {
        /* Show a progress dialog and start the Loader for the login. */
        showProgress(true);

        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_PASSWORD, password);

        getSupportLoaderManager()
                .restartLoader(LOADER_LOGIN_ID, args, this)
                .forceLoad();
    }

    @Override
    public void onLoginAsGuest() {
        showPersonalDataView();
    }

    @Override
    public void onPersonalDataComplete(User user) {
        /* Show a progress dialog and start the Loader for the login. */
        showProgress(true);

        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, user.getUsername());
        args.putString(ARG_PASSWORD, user.getPassword());
        args.putString(ARG_NAME, user.getRealName());

        getSupportLoaderManager()
                .initLoader(LOADER_FAKE_LOGIN_ID, args, this)
                .forceLoad();
    }




    @NonNull
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_LOGIN_ID:
                /*
                 * Get the username and password inserted by the user, create an
                 * authentic user, then start the login process.
                 */
                return mViewModel.doLogin(this,
                        args.getString(ARG_USERNAME), args.getString(ARG_PASSWORD));

            case LOADER_FAKE_LOGIN_ID:
                /* Create a fake user and start the fake login process. */
                return mViewModel.doFakeLogin(this,
                        args.getString(ARG_USERNAME), args.getString(ARG_PASSWORD),
                        args.getString(ARG_NAME));

            case LOADER_CONFIRM_FACULTY_ID:
                /* Get the user, save the chosen faculty, then complete the login process. */
                return mViewModel.downloadUserData(this);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        int loader_id = loader.getId();

        switch (loader_id) {
            case LOADER_LOGIN_ID:
            case LOADER_FAKE_LOGIN_ID:
            case LOADER_CONFIRM_FACULTY_ID:
                handleLoginResults((User) data);
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showProgress(false);
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void getAccountPermission() {
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)) {
            Snackbar.make(
                    mBinding.loginView,
                    R.string.permissions_granted,
                    Snackbar.LENGTH_LONG).show();
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.error_permission_accounts_necessary),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    android.Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {}
}

