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
import android.support.design.widget.Snackbar;


import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
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
        LoaderManager.LoaderCallbacks, EasyPermissions.PermissionCallbacks {

    private ActivityLoginBinding mBinding;

    /* */
    @Inject
    LoginViewModelFactory viewModelFactory;

    /* */
    private LoginViewModel mViewModel;

    private AccountManager mAccountManager;
    private String username;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1001;

    private static final int LOADER_LOGIN_ID = 2100;
    private static final int LOADER_FAKE_LOGIN_ID = 2101;
    private static final int LOADER_CONFIRM_FACULTY_ID = 2102;

    private ProgressDialog progress;

    private int selected = Spinner.INVALID_POSITION;
    private int selectedFaculty = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(LoginViewModel .class);

        initUI();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
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

            if (S3_accounts.length == 0)
                mBinding.buttonLogin.setOnClickListener(v -> attemptLogin());
            else {
                User user = mViewModel.getUser();

                if (!user.isAuthTokenSet()) {
                    mBinding.buttonLogin.setOnClickListener(v -> attemptLogin());
                }
                else if (user.hasMultiFaculty() && !user.isFacultyChosen()) {
                    hideMainLoginView();
                    showFacultyChoiceView(user, this);
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

                getSupportLoaderManager()
                        .initLoader(LOADER_FAKE_LOGIN_ID, null, this)
                        .forceLoad();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {}

    private void initUI() {
        setSupportActionBar(mBinding.toolbar);

        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        showMainLoginView();
        hideFacultyChoiceView();

        initTermsCheck();

        mBinding.buttonLogin.setOnClickListener(view -> attemptLogin());

        mBinding.coursesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = position;

                Log.d("LOGIN_CHOOSE_FACULTY", "New faculty selected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.label_logging_in));
        progress.setCancelable(false);

        mBinding.toolbar.setTitle(R.string.title_login);
    }

    private void initTermsCheck() {
        String text = getString(R.string.terms_and_conditions);
        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW, Uri.parse(Utils.TERMS_CONDITIONS_URL));
                startActivity(browserIntent);
            }
        };
        ss.setSpan(clickableSpan,
                getResources().getInteger(R.integer.start_terms_and_conditions),
                getResources().getInteger(R.integer.end_terms_and_conditions),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mBinding.termsCheck.setText(ss);
        mBinding.termsCheck.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();

        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            username = appLinkData.getLastPathSegment();
            mBinding.usernameEdittext.setText(username);
        }
    }

    private void showProgress(final boolean show) {
        if (show) progress.show();
        else if(progress != null && progress.isShowing()) progress.dismiss();
    }




    @SuppressWarnings("unchecked")
    private void handleLoginResults(final Context context, final User user){
        showProgress(false);

        /* Get the login result */
        int ris = (int) user.getTag();
        switch (ris){

            /* If the user has to choose the faculty */
            case S3Helper.ERROR_FACULTY_TO_CHOOSE:

                /* Update the UI */
                hideMainLoginView();
                showFacultyChoiceView(user, context);

                /* Signal the user */
                Snackbar.make(mBinding.loginView,
                        R.string.faculty_to_choose, Snackbar.LENGTH_LONG).show();
                break;

            /* If the login process is completed successfully */
            case S3Helper.OK_LOGGED_IN:
            case S3Helper.OK_UPDATED:
                /* Finalize login process */
                finishLogin();
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
                mBinding.passwordTextInputLayout.setError(getString(R.string.error_username_password_incorrect));
                mBinding.passwordTextInputLayout.requestFocus();
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

    private void finishLogin() {
        resetView();

        /* Since the user is logged in, start the Main Activity */
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void resetView() {
        mBinding.usernameEdittext.requestFocus();
        mBinding.usernameEdittext.setText("");
        mBinding.passwordEdittext.setText("");
        mBinding.termsCheck.setChecked(false);

        hideFacultyChoiceView();
        showMainLoginView();
    }

    @SuppressWarnings("unchecked")
    private void attemptLogin() {

        /* If the user has accepted the Terms and Conditions */
        if (mBinding.termsCheck.isChecked()) {

            /* If the device is online */
            if (NetworkHelper.isDeviceOnline(this)) {
                Utils.hideKeyboard(this);

                /* Reset errors. */
                mBinding.usernameTextInputLayout.setError(null);
                mBinding.passwordTextInputLayout.setError(null);

                boolean cancel = false;
                View focusView = null;

                /* Check for a valid password, if the user entered one. */
                if (!isPasswordValid(mBinding.passwordTextInputLayout)) {
                    focusView = mBinding.passwordTextInputLayout;
                    cancel = true;
                }

                /* Check for a valid username, if the user entered one. */
                username = validateUsername(mBinding.usernameTextInputLayout);
                if (username == null) {
                    focusView = mBinding.usernameTextInputLayout;
                    cancel = true;
                }

                /* If there are errors to show */
                if (cancel) {
                    /* Cancel the login process and focus on the field with an error. */
                    focusView.requestFocus();
                }
                /* No errors found */
                else {
                    /* Show a progress dialog and start the Loader for the login. */
                    showProgress(true);

                    getSupportLoaderManager()
                            .restartLoader(LOADER_LOGIN_ID, null, this)
                            .forceLoad();
                }
            }
            else
                /* Tell the user that there is no internet available. */
                Snackbar.make(
                        mBinding.loginView,
                        R.string.error_no_internet,
                        Snackbar.LENGTH_LONG).show();
        }
        /* Tell the user that is necessary to accept the Terms and Conditions. */
        else {
            mBinding.termsCheck.setError(getString(R.string.terms_not_checked));
            mBinding.termsCheck.requestFocus();
        }
    }

    @Nullable
    private String validateUsername(@Nullable TextInputLayout view) {
        /* If there is no view to work with, return */
        if (view == null || view.getEditText() == null) return null;

        /* Get the username */
        String user = view.getEditText().getText().toString().trim();

        /* Check the username for errors */
        if (TextUtils.isEmpty(user)){
            /* The username is empty, set the error and return. */
            user = null;
            view.setError(getString(R.string.error_user_empty));
        } else if (user.contains(" ")) {
            /* The username contains whitespaces, set the error and return. */
            user = null;
            view.setError(getString(R.string.error_user_with_blank_spaces));
        } else if (user.contains("@")) {
            /*
             * The user has input the university email, but the app only needs the username,
             * so remove everything that is after the '@', including '@', and return.
             */
            user = user.substring(0, user.indexOf("@"));
        }

        return user;
    }

    private boolean isPasswordValid(@Nullable TextInputLayout view) {
        if (view == null || view.getEditText() == null) return false;

        String password = view.getEditText().getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(password)){
            valid = false;
            view.setError(getString(R.string.error_password_empty));
        } else if (password.length() < 10){
            valid = false;
            view.setError(getString(R.string.error_password_less_then_10_chars));
        } else if (password.length() > 20){
            valid = false;
            view.setError(getString(R.string.error_password_more_then_20_chars));
        } else if (!password.matches("(.)*[0-9](.)*[0-9](.)*")){
            valid = false;
            view.setError(getString(R.string.error_password_less_then_2_digits));
        }

        return valid;
    }

    private void showFacultyChoiceView(User user, Context context) {
        mBinding.coursesLabel.setVisibility(View.VISIBLE);
        mBinding.coursesSpinner.setVisibility(View.VISIBLE);
        mBinding.dialogButtonOK.setVisibility(View.VISIBLE);

        /* Load the faculties */
        ArrayList<String> courses_names = new ArrayList<>();
        for (int i=0; i<user.getFaculties().size(); i++)
            courses_names.add(user.getFaculties().valueAt(i));

        /* Show the faculties list */
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.simple_spinner_item, courses_names);
        mBinding.coursesSpinner.setAdapter(adapter);

        /* When the user has chosen the faculty */
        mBinding.dialogButtonOK.setOnClickListener(v -> {
            /* Save the chosen faculty */
            selectedFaculty = user.getFaculties().keyAt(selected);

            /* Do the login again */
            getSupportLoaderManager()
                    .initLoader(LOADER_CONFIRM_FACULTY_ID, null, LoginActivity.this)
                    .forceLoad();

            showProgress(true);
        });
    }

    private void hideFacultyChoiceView() {
        mBinding.coursesLabel.setVisibility(View.INVISIBLE);
        mBinding.coursesSpinner.setVisibility(View.INVISIBLE);
        mBinding.dialogButtonOK.setVisibility(View.INVISIBLE);
    }

    private void showMainLoginView() {
        mBinding.usernameTextInputLayout.setVisibility(View.VISIBLE);
        mBinding.passwordTextInputLayout.setVisibility(View.VISIBLE);
        mBinding.termsCheck.setVisibility(View.VISIBLE);
        mBinding.buttonLogin.setVisibility(View.VISIBLE);
    }

    private void hideMainLoginView() {
        mBinding.usernameTextInputLayout.setVisibility(View.GONE);
        mBinding.passwordTextInputLayout.setVisibility(View.GONE);
        mBinding.termsCheck.setVisibility(View.GONE);
        mBinding.buttonLogin.setVisibility(View.GONE);
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_LOGIN_ID:
                /*
                 * Get the username and password inserted by the user, create an
                 * authentic user, then start the login process.
                 */
                username = mBinding.usernameEdittext.getText().toString();
                String password = mBinding.passwordEdittext.getText().toString();
                return mViewModel.doLogin(this, username, password);

            case LOADER_FAKE_LOGIN_ID:
                /* Create a fake user and start the fake login process. */
                return mViewModel.doFakeLogin(this);

            case LOADER_CONFIRM_FACULTY_ID:
                /* Get the user, save the chosen faculty, then complete the login process. */
                return mViewModel.downloadUserData(this, selectedFaculty);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        int loader_id = loader.getId();

        switch (loader_id) {
            case LOADER_LOGIN_ID:
            case LOADER_FAKE_LOGIN_ID:
            case LOADER_CONFIRM_FACULTY_ID:
                handleLoginResults(this, (User) data);
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {}



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
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // Do nothing
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Do nothing
    }
}
