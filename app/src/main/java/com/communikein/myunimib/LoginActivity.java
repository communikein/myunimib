package com.communikein.myunimib;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.communikein.myunimib.accountmanager.AccountUtils;
import com.communikein.myunimib.databinding.ActivityLoginBinding;
import com.communikein.myunimib.sync.S3Helper;
import com.communikein.myunimib.utilities.NetworkUtils;
import com.communikein.myunimib.utilities.UserUtils;
import com.communikein.myunimib.utilities.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends AccountAuthenticatorActivity implements
        LoaderManager.LoaderCallbacks, EasyPermissions.PermissionCallbacks {

    private ActivityLoginBinding mBinding;

    private AccountManager mAccountManager;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1001;

    private static final int LOADER_LOGIN_ID = 2100;
    private static final int LOADER_CONFIRM_FACULTY_ID = 2101;

    private ProgressDialog progress;

    int selectedCourse = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAccountManager == null) mAccountManager = AccountManager.get(this);

        // Check if the app has the permission to access the accounts saved on the device
        if (EasyPermissions.hasPermissions(this,
                android.Manifest.permission.GET_ACCOUNTS, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Check if there already is a registered ic_account on the device
            Account[] S3_accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);

            if (S3_accounts.length == 0)
                mBinding.buttonLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptLogin();
                    }
                });
            else
                startActivity(new Intent(this, MainActivity.class));
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

    public void initUI() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        mBinding.editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mBinding.buttonLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mBinding.coursesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = position;

                Log.d("LOGIN_CHOOSE_FACULTY", "New faculty selected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.title_logging_in));
        progress.setCancelable(false);
    }

    private void handleLoginResults(final Context context, final User user){
        showProgress(false);

        int ris = (int) user.getTag();
        switch (ris){
            // If the user has to choose the faculty
            case S3Helper.ERROR_FACULTY_TO_CHOOSE:
                // Tell the user he needs to choose the faculty
                Snackbar.make(mBinding.loginView, R.string.faculty_to_choose, Snackbar.LENGTH_LONG)
                        .show();

                // Carico l'elenco dei corsi di studio
                ArrayList<String> courses_names = new ArrayList<>();
                for (int i=0; i<user.getFaculties().size(); i++)
                    courses_names.add(user.getFaculties().valueAt(i));

                // Imposto nello Spinner i corsi di studio disponibili
                final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                        R.layout.simple_spinner_item, courses_names);
                mBinding.coursesSpinner.setAdapter(adapter);

                Log.d("LOGIN_CHOOSE_FACULTY", "Faculties shown to user. Waiting for user input.");

                // Quando l'utente seleziona il corso di studio e da conferma
                mBinding.dialogButtonOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int selected = user.getFaculties().keyAt(selectedCourse);

                        Log.d("LOGIN_CHOOSE_FACULTY", "Faculty chosen: " + selected);

                        // Set and save the chosen faculty
                        user.setSelectedFaculty(selected);
                        UserUtils.saveUser(user, getBaseContext());

                        // Rieffettuo il login, con il corso di studio selezionato
                        Log.d("LOGIN_CHOOSE_FACULTY", "Trying to tell the server.");

                        getLoaderManager()
                                .initLoader(LOADER_CONFIRM_FACULTY_ID,
                                        null,
                                        LoginActivity.this)
                                .forceLoad();

                        showProgress(true);
                    }
                });
                break;
            // Se il login ha avuto successo
            case S3Helper.OK_LOGGED_IN:
                // Finalize login process
                finishLogin(user);
                break;

            // Se S3 non è disponibile
            case S3Helper.ERROR_S3_NOT_AVAILABLE:
                // Avviso l'utente
                Snackbar.make(mBinding.loginView, R.string.error_S3_not_available, Snackbar.LENGTH_LONG)
                        .show();
                break;

            // Se ho sbagliato password
            case S3Helper.ERROR_WRONG_PASSWORD:
                // Avviso l'utente
                Snackbar.make(mBinding.loginView, R.string.error_password_incorrect, Snackbar.LENGTH_LONG)
                        .show();

                mBinding.editTextPassword.setError(getString(R.string.error_password_incorrect));
                mBinding.editTextPassword.requestFocus();
                break;

            // Se la connessione sta impiegando troppo tempo
            case S3Helper.ERROR_CONNECTION_TIMEOUT:
                // Avviso l'utente
                Snackbar.make(mBinding.loginView, R.string.error_connection_timeout, Snackbar.LENGTH_LONG)
                        .show();
                break;

            // For any other response
            default:
                Snackbar.make(mBinding.loginView, R.string.error_generic_exception, Snackbar.LENGTH_LONG)
                        .show();
                break;
        }
    }

    private void finishLogin(User user) {
        final Account account = new Account(user.getUsername(), AccountUtils.ACCOUNT_TYPE);

        Bundle bundle = new Bundle();
        bundle.putString(User.PREF_USERNAME, user.getUsername());
        bundle.putString(User.PREF_MATRICOLA, user.getMatricola());
        mAccountManager.addAccountExplicitly(account, user.getPassword(), bundle);

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, user.getUsername());
        data.putString(User.PREF_USERNAME, user.getUsername());
        data.putString(User.PREF_PASSWORD, user.getPassword());
        data.putString(User.PREF_MATRICOLA, user.getMatricola());
        setAccountAuthenticatorResult(data);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void attemptLogin() {
        if (mBinding.termsCheck.isChecked()) {
            if (NetworkUtils.isDeviceOnline(this)) {
                Utils.hideKeyboard(this, true);

                // Reset errors.
                mBinding.editTextUniversityMail.setError(null);
                mBinding.editTextPassword.setError(null);

                boolean cancel = false;
                View focusView = null;

                // Check for a valid password, if the user entered one.
                if (!isPasswordValid(mBinding.editTextPassword)) {
                    focusView = mBinding.editTextPassword;
                    cancel = true;
                }

                // Check for a valid username.
                if (validateUsername(mBinding.editTextUniversityMail) == null) {
                    focusView = mBinding.editTextUniversityMail;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    showProgress(true);

                    getLoaderManager()
                            .initLoader(LOADER_LOGIN_ID, null, this)
                            .forceLoad();
                }
            }
            else
                Snackbar.make(
                        mBinding.loginView,
                        R.string.error_no_internet,
                        Snackbar.LENGTH_LONG).show();
        } else {
            mBinding.termsCheck.setError(getString(R.string.terms_not_checked));
            mBinding.termsCheck.requestFocus();
        }
    }

    private String validateUsername(TextView view) {
        String user = view.getText().toString().trim();

        if (TextUtils.isEmpty(user)){
            user = null;
            view.setError(getString(R.string.error_user_empty));
        } else if (user.contains(" ")) {
            user = null;
            view.setError(getString(R.string.error_user_with_blank_spaces));
        } else if (user.contains("@")) {
            user = user.substring(0, user.indexOf("@"));
        }

        return user;
    }

    private boolean isPasswordValid(TextView view) {
        String password = view.getText().toString().trim();
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

    private void showProgress(final boolean show) {
        if (show) progress.show();
        else if(progress != null && progress.isShowing()) progress.dismiss();
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_LOGIN_ID:
                String username = mBinding.editTextUniversityMail.getText().toString();
                String password = mBinding.editTextPassword.getText().toString();

                return new S3Helper.LoginLoader(this, username, password);

            case LOADER_CONFIRM_FACULTY_ID:
                return new S3Helper.ConfirmFacultyLoader(this, UserUtils.getUser(this));

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        int loader_id = loader.getId();

        switch (loader_id) {
            case LOADER_LOGIN_ID:
                handleLoginResults(this, (User) data);
                break;

            case LOADER_CONFIRM_FACULTY_ID:
                // Salvo i dati scaricati dell'utente attuale su dispositivo
                UserUtils.saveUser(Utils.user, this);

                handleLoginResults(this, (User) data);
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }



    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void getAccountPermission() {
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)) {
            if (!TextUtils.isEmpty(mBinding.editTextPassword.getText())){
                Snackbar.make(mBinding.loginView, R.string.permissions_granted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptLogin();
                    }
                }).show();
            }
            else {
                Snackbar.make(
                        mBinding.loginView,
                        R.string.permissions_granted,
                        Snackbar.LENGTH_LONG).show();
            }
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

