package it.communikein.myunimib.ui.exam.available;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.data.network.loaders.EnrollLoader;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.databinding.ActivityAvailableExamDetailsBinding;
import it.communikein.myunimib.utilities.Utils;
import it.communikein.myunimib.viewmodel.AvailableExamDetailViewModel;
import it.communikein.myunimib.viewmodel.factory.AvailableExamViewModelFactory;


public class AvailableExamDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks, EnrollLoader.EnrollUpdatesListener,
        HasActivityInjector {

    private final static int LOADER_ENROLL_ID = 4000;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    /* */
    private ActivityAvailableExamDetailsBinding mBinding;

    /* */
    @Inject
    AvailableExamViewModelFactory viewModelFactory;

    /* */
    private AvailableExamDetailViewModel mViewModel;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_available_exam_details);

        ExamID examID = loadData();

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(AvailableExamDetailViewModel.class);
        mViewModel.setExamId(examID);

        mBinding.setLifecycleOwner(this);
        mBinding.setExam(mViewModel.getExam());

        initUI();
    }

    private ExamID loadData() {
        if (getIntent() != null) {
            int adsceId = getIntent().getIntExtra(UnimibNetworkDataSource.ADSCE_ID, -1);
            int appId = getIntent().getIntExtra(UnimibNetworkDataSource.APP_ID, -1);
            int attDidEsaId = getIntent().getIntExtra(UnimibNetworkDataSource.ATT_DID_ESA_ID, -1);
            int cdsEsaId = getIntent().getIntExtra(UnimibNetworkDataSource.CDS_ESA_ID, -1);

            return new ExamID(cdsEsaId, attDidEsaId, appId, adsceId);
        }

        return null;
    }

    private void initUI() {
        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        initFab();
        initToolbar();

        mViewModel.getExam().observe(this, this::updateUI);
    }

    private void initFab(){
        mBinding.examEnrollFab.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorAccent)));
        mBinding.examEnrollFab.setVisibility(View.INVISIBLE);
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void updateUI(AvailableExam exam) {
        if (exam != null) {
            updateToolbar(exam);
            updateFab();
        }
    }

    private void updateToolbar(AvailableExam exam) {
        if (exam != null)
            mBinding.toolbarLayout.setTitle(exam.getName());
    }

    private void updateFab() {
        mBinding.examEnrollFab.setVisibility(View.VISIBLE);
        mBinding.examEnrollFab.setOnClickListener(v -> tryEnroll());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);

        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ENROLL_ID:
                showProgress(true);

                return mViewModel.enroll(this, this);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object result) {
        showProgress(false);

        switch (loader.getId()) {
            case LOADER_ENROLL_ID:
                handleEnrollExam((boolean) result);
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {}

    @Override
    public void onEnrollmentUpdate(int status) {
        switch (status) {
            case EnrollLoader.STATUS_STARTED:
                Snackbar.make(mBinding.container,
                        R.string.label_enrollment_started, Snackbar.LENGTH_LONG).show();
                break;

            case EnrollLoader.STATUS_ENROLLMENT_OK:
                Snackbar.make(mBinding.container,
                        R.string.label_enrollment_confirmed, Snackbar.LENGTH_LONG).show();
                break;

            case EnrollLoader.STATUS_CERTIFICATE_DOWNLOADED:
                Snackbar.make(mBinding.container,
                        R.string.label_certificate_downloaded, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open, v -> showCertificate())
                        .show();
                break;

            case EnrollLoader.STATUS_ERROR_QUESTIONNAIRE_TO_FILL:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_questionnaire)
                        .setMessage(R.string.error_questionnaire_to_fill)
                        .setPositiveButton(R.string.action_show_questionnaire,
                                (dialog, which) -> showUnimibWebsite())
                        .setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;

            case EnrollLoader.STATUS_ERROR_CERTIFICATE:
                Snackbar.make(mBinding.container,
                        R.string.error_certificate_not_found, Snackbar.LENGTH_LONG).show();
                break;

            case EnrollLoader.STATUS_ERROR_GENERAL:
                Snackbar.make(mBinding.container,
                        R.string.error_generic, Snackbar.LENGTH_LONG).show();
                break;
        }
    }


    private void tryEnroll() {
        new AlertDialog.Builder(getBaseContext())
            .setTitle(getString(R.string.attention_title))
            .setMessage(R.string.prompt_enrollment_confirm)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> doEnroll())
            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    @SuppressWarnings("unchecked")
    private void doEnroll() {
        progress.setMessage(getString(R.string.label_enrollment_in_progress));
        showProgress(true);

        getSupportLoaderManager()
                .initLoader(LOADER_ENROLL_ID, null, this)
                .forceLoad();
    }

    private void handleEnrollExam(boolean enrolled) {
        showProgress(false);

        if (enrolled) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_enrollment_completed)
                    .setMessage(R.string.enrollment_completed_info)
                    .setPositiveButton(R.string.action_go, (dialog, which) -> showCertificate())
                    .setNegativeButton(R.string.action_ok, (dialog, which) -> finish())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            mViewModel.refreshAvailableExams();
            mViewModel.refreshEnrolledExams();
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.attention_title)
                    .setMessage(R.string.enrollment_not_completed)
                    .setNeutralButton(R.string.action_ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void showUnimibWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.UNIMIB_WEBSITE));
        startActivity(browserIntent);
    }

    private void showCertificate() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (mViewModel.getExam().getValue() != null) {
            File certificate_file = mViewModel.getExam().getValue().getCertificatePath();
            Uri certificate_uri = FileProvider.getUriForFile(this,
                    getString(R.string.file_provider_authority), certificate_file);

            intent.setDataAndType(certificate_uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    private void showProgress(final boolean show) {
        if (show) progress.show();
        else
        if(progress != null && progress.isShowing()) progress.dismiss();
    }



    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
