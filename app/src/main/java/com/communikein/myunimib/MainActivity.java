package com.communikein.myunimib;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.communikein.myunimib.databinding.ActivityMainBinding;
import com.communikein.myunimib.sync.S3Helper;
import com.communikein.myunimib.utilities.UserUtils;
import com.communikein.myunimib.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks {

    private ActivityMainBinding mBinding;

    private final List<Fragment> fragments = new ArrayList<>();

    private static final int INDEX_FRAGMENT_HOME = 0;
    private static final int INDEX_FRAGMENT_BOOKLET = 1;
    private static final int INDEX_FRAGMENT_EXAMS_AVAILABLE = 2;
    private static final int INDEX_FRAGMENT_EXAMS_ENROLLED = 3;

    private static final String TAG_FRAGMENT_HOME = "tab-home";
    private static final String TAG_FRAGMENT_BOOKLET = "tab-booklet";
    private static final String TAG_FRAGMENT_EXAMS_AVAILABLE = "tab-exams-available";
    private static final String TAG_FRAGMENT_EXAMS_ENROLLED = "tab-exams-enrolled";

    private static final int LOADER_LOGOUT_ID = 2200;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initUI();
    }

    private void initUI(){
        buildFragmentsList();

        switchFragment(R.id.navigation_home);

        mBinding.navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return switchFragment(item.getItemId());
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.label_logging_out));
        progressDialog.setCancelable(false);
    }

    private void buildFragmentsList() {
        fragments.add(new HomeFragment());
        fragments.add(new BookletFragment());
        fragments.add(new ExamsAvailableFragment());
        fragments.add(new ExamsEnrolledFragment());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_logout:
                showProgress(true);

                getLoaderManager()
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
                showProgress(false);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.dialog_logout));
                if (data != null) {
                    builder.setMessage(getString(R.string.dialog_logout_message_ok));

                    String positiveText = getString(android.R.string.ok);
                    builder.setPositiveButton(positiveText,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                }
                else {
                    builder.setMessage(getString(R.string.dialog_logout_message_ok));

                    String positiveText = getString(android.R.string.ok);
                    builder.setPositiveButton(positiveText, null);
                }
                AlertDialog dialog = builder.create();
                dialog.show();

                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void showProgress(final boolean show) {
        if (show) progressDialog.show();
        else if(progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }
}
