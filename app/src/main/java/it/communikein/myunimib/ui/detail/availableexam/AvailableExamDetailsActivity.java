package it.communikein.myunimib.ui.detail.availableexam;

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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.AvailableExam;
import it.communikein.myunimib.data.database.ExamID;
import it.communikein.myunimib.data.network.S3Helper;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.databinding.ActivityAvailableExamDetailsBinding;
import it.communikein.myunimib.utilities.InjectorUtils;
import it.communikein.myunimib.utilities.MyunimibDateUtils;


public class AvailableExamDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks, S3Helper.EnrollLoader.EnrollUpdatesListener {

    final public static int LOADER_ENROLL_ID = 4000;

    private ActivityAvailableExamDetailsBinding mBinding;
    private AvailableExamDetailViewModel mViewModel;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_available_exam_details);

        ExamID examID = loadData();
        if (examID != null)
            initUI(examID);
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

    private void initUI(ExamID examID) {
        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        initFab();
        initToolbar();

        AppExecutors.getInstance().diskIO().execute(() -> {
            AvailableExamViewModelFactory factory = InjectorUtils
                    .provideAvailableExamViewModelFactory(this, examID);
            mViewModel = ViewModelProviders.of(this, factory)
                    .get(AvailableExamDetailViewModel.class);

            updateUI(mViewModel.getExam());
        });
    }

    private void initFab(){
        mBinding.examEnrollFab.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorAccent)));
        mBinding.examEnrollFab.setVisibility(View.INVISIBLE);
    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }


    private void updateUI(AvailableExam exam) {
        if (exam != null) {
            String friendly_date_begin = MyunimibDateUtils.getFriendlyDateString(
                    this,
                    exam.getBeginEnrollment().getTime(),
                    false,
                    false);
            String friendly_date_end = MyunimibDateUtils.getFriendlyDateString(
                    this,
                    exam.getEndEnrollment().getTime(),
                    false,
                    false);
            String friendly_date = MyunimibDateUtils.getFriendlyDateString(
                    this,
                    exam.getDate().getTime(),
                    false,
                    false);

            mBinding.examBeginEnrollmentTextview.setText(friendly_date_begin);
            mBinding.examEndEnrollmentTextview.setText(friendly_date_end);
            mBinding.examDateTextview.setText(friendly_date);
            mBinding.examDescriptionTextview.setText(exam.getDescription());

            updateToolbar(exam);
            updateFab();
        }
    }

    private void updateToolbar(AvailableExam exam) {
        /* Get a reference to the MainActivity ActionBar */
        ActionBar actionBar = getSupportActionBar();
        /* If there is an ActionBar, set it's title */
        if (actionBar != null && exam != null)
            actionBar.setTitle(exam.getName());
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

                return new S3Helper.EnrollLoader(this, mViewModel.getExam(), this);

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
            case S3Helper.EnrollLoader.STATUS_STARTED:
                Snackbar.make(mBinding.container,
                        "Enrollment started.", Snackbar.LENGTH_LONG).show();
                break;

            case S3Helper.EnrollLoader.STATUS_ENROLLMENT_OK:
                Snackbar.make(mBinding.container,
                        "Enrollment confirmed.", Snackbar.LENGTH_LONG).show();
                break;

            case S3Helper.EnrollLoader.STATUS_CERTIFICATE_DOWNLOADED:
                Snackbar.make(mBinding.container,
                        "Certificate downloaded.", Snackbar.LENGTH_LONG)
                        .setAction(R.string.open, v -> showCertificate())
                        .show();
                break;

            case S3Helper.EnrollLoader.STATUS_ERROR_QUESTIONNAIRE_TO_FILL:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_questionnaire)
                        .setMessage(R.string.error_questionnaire_to_fill)
                        .setPositiveButton(R.string.action_show_questionnaire,
                                (dialog, which) -> showUnimibWebsite())
                        .setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;

            case S3Helper.EnrollLoader.STATUS_ERROR_CERTIFICATE:
                Snackbar.make(mBinding.container,
                        "ERROR: certificate not found.", Snackbar.LENGTH_LONG).show();
                break;

            case S3Helper.EnrollLoader.STATUS_ERROR_GENERAL:
                Snackbar.make(mBinding.container,
                        "ERROR: general.", Snackbar.LENGTH_LONG).show();
                break;
        }
    }


    private void tryEnroll() {
        new AlertDialog.Builder(getBaseContext())
            .setTitle(getString(R.string.attention_title))
            .setMessage("Sicuro di voler procedere con la prenotazione dell'esame?")
            .setPositiveButton(android.R.string.ok, (dialog, which) -> doEnroll())
            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    @SuppressWarnings("unchecked")
    private void doEnroll() {
        progress.setMessage("Sto prenotando l'esame..");
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

            UnimibNetworkDataSource.getInstance(this, AppExecutors.getInstance())
                    .startFetchAvailableExamsService();
            UnimibNetworkDataSource.getInstance(this, AppExecutors.getInstance())
                    .startFetchEnrolledExamsService();
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
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://s3w.si.unimib.it/esse3/"));
        startActivity(browserIntent);
    }

    private void showCertificate() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File certificate_file = mViewModel.getExam().getCertificatePath();
        Uri certificate_uri = FileProvider.getUriForFile(this,
                getString(R.string.file_provider_authority), certificate_file);

        intent.setDataAndType(certificate_uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void showProgress(final boolean show) {
        if (show) progress.show();
        else
        if(progress != null && progress.isShowing()) progress.dismiss();
    }
}
