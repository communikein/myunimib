package it.communikein.myunimib.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import it.communikein.myunimib.R;
import it.communikein.myunimib.accountmanager.AccountUtils;
import it.communikein.myunimib.databinding.ActivityMainBinding;
import it.communikein.myunimib.data.network.S3Helper;
import it.communikein.myunimib.ui.detail.HomeFragment;
import it.communikein.myunimib.ui.list.availableexam.AvailableExamsFragment;
import it.communikein.myunimib.ui.list.booklet.BookletFragment;
import it.communikein.myunimib.ui.list.enrolledexam.EnrolledExamsFragment;
import it.communikein.myunimib.utilities.UserUtils;
import it.communikein.myunimib.utilities.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding mBinding;

    private final List<Fragment> fragments = new ArrayList<>();

    public static final String INTENT_PARAM_SHOW_FRAGMENT = "show-fragment";

    private static final int INDEX_FRAGMENT_HOME = 0;
    private static final int INDEX_FRAGMENT_BOOKLET = 1;
    private static final int INDEX_FRAGMENT_EXAMS_AVAILABLE = 2;
    private static final int INDEX_FRAGMENT_EXAMS_ENROLLED = 3;

    public static final String TAG_FRAGMENT_HOME = "tab-home";
    public static final String TAG_FRAGMENT_BOOKLET = "tab-booklet";
    public static final String TAG_FRAGMENT_EXAMS_AVAILABLE = "tab-exams-available";
    public static final String TAG_FRAGMENT_EXAMS_ENROLLED = "tab-exams-enrolled";

    private static final int LOADER_LOGOUT_ID = 2200;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initUI();
        parseIntent();
    }

    private void initUI(){
        buildFragmentsList();

        mBinding.navigation.setOnNavigationItemSelectedListener(item ->
                switchFragment(item.getItemId()));
        mBinding.navigation.setSelectedItemId(R.id.navigation_home);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.label_logging_out));
        progressDialog.setCancelable(false);
    }

    private void parseIntent() {
        Intent intent = getIntent();

        if (intent != null) {
            int nav_id = intent.getIntExtra(INTENT_PARAM_SHOW_FRAGMENT, -1);

            if (nav_id >= 0)
                mBinding.navigation.setSelectedItemId(nav_id);
        }
    }

    private void buildFragmentsList() {
        fragments.add(INDEX_FRAGMENT_HOME, new HomeFragment());
        fragments.add(INDEX_FRAGMENT_BOOKLET, new BookletFragment());
        fragments.add(INDEX_FRAGMENT_EXAMS_AVAILABLE, new AvailableExamsFragment());
        fragments.add(INDEX_FRAGMENT_EXAMS_ENROLLED, new EnrolledExamsFragment());
    }

    private boolean switchFragment(int tab_id) {
        int index;
        String tag;

        switch (tab_id) {
            case R.id.navigation_home:
                index = INDEX_FRAGMENT_HOME;
                tag = TAG_FRAGMENT_HOME;
                break;
            case R.id.navigation_booklet:
                index = INDEX_FRAGMENT_BOOKLET;
                tag = TAG_FRAGMENT_BOOKLET;
                break;
            case R.id.navigation_exams_available:
                index = INDEX_FRAGMENT_EXAMS_AVAILABLE;
                tag = TAG_FRAGMENT_EXAMS_AVAILABLE;
                break;
            case R.id.navigation_exams_enrolled:
                index = INDEX_FRAGMENT_EXAMS_ENROLLED;
                tag = TAG_FRAGMENT_EXAMS_ENROLLED;
                break;
            default:
                return false;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.tab_container, fragments.get(index), tag)
                .commit();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_logout:
                showProgress(true);

                getSupportLoaderManager()
                        .initLoader(LOADER_LOGOUT_ID, null, this)
                        .forceLoad();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_LOGOUT_ID:
                Utils.user = UserUtils.getUser(this);
                return new S3Helper.LogoutLoader(this, Utils.user);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        int loader_id = loader.getId();

        switch (loader_id) {
            case LOADER_LOGOUT_ID:
                if (data != null)
                    finishLogout();
                else
                    showLogoutErrorDialog(null);

                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {}


    private void finishLogout() {
        final AccountManager accountManager = AccountManager.get(this);
        final Account account = accountManager
                .getAccountsByType(AccountUtils.ACCOUNT_TYPE)[0];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            /*
             * Trying to call this on an older Android version results in a
             * NoSuchMethodError exception. There is no AppCompat version of the
             * AccountManager API to avoid the need for this version check at runtime.
             */
            accountManager.removeAccount(account, MainActivity.this,
                    accountManagerFuture -> {
                        showProgress(false);

                        boolean isRemoved = false;
                        try {
                            Bundle data = accountManagerFuture.getResult();
                            isRemoved = data.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
                        } catch (IOException e) {
                            Utils.saveBugReport(e, TAG);
                            showLogoutErrorDialog(null);
                        } catch (OperationCanceledException e) {
                            Utils.saveBugReport(e, TAG);

                            String error = getString(R.string.error_logout_cancelled);
                            showLogoutErrorDialog(error);
                        } catch (AuthenticatorException e) {
                            Utils.saveBugReport(e, TAG);
                            showLogoutErrorDialog(null);
                        }

                        if (isRemoved) {
                            showLogoutCompletedDialog();
                        }
                        else {
                            String error = getString(R.string.error_logout_user_not_removed);
                            showLogoutErrorDialog(error);
                        }
                    }, null);
        } else {
            /* Note that this needs the MANAGE_ACCOUNT permission on SDK <= 22. */
            accountManager.removeAccount(account, accountManagerFuture -> {
                showProgress(false);

                boolean isRemoved = false;
                try {
                    isRemoved = accountManagerFuture.getResult();
                } catch (IOException e) {
                    Utils.saveBugReport(e, TAG);
                    showLogoutErrorDialog(null);
                } catch (OperationCanceledException e) {
                    Utils.saveBugReport(e, TAG);

                    String error = getString(R.string.error_logout_cancelled);
                    showLogoutErrorDialog(error);
                } catch (AuthenticatorException e) {
                    Utils.saveBugReport(e, TAG);
                    showLogoutErrorDialog(null);
                }

                if (isRemoved) {
                    showLogoutCompletedDialog();
                }
                else {
                    String error = getString(R.string.error_logout_user_not_removed);
                    showLogoutErrorDialog(error);
                }
            }, null);
        }
    }

    private void showLogoutCompletedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_logout));
        builder.setMessage(getString(R.string.dialog_logout_message_ok));
        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText, (dialog, which) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLogoutErrorDialog(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_logout));

        if (error == null) error = getString(R.string.error_logout_failed);
        builder.setMessage(error);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showProgress(final boolean show) {
        if (show) progressDialog.show();
        else if(progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    public PendingIntent buildPendingIntent(int navigation_tab_id) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(INTENT_PARAM_SHOW_FRAGMENT, navigation_tab_id);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
