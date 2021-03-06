package it.communikein.myunimib.ui.exam.available;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.Exam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.data.network.loaders.EnrollLoader;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.databinding.FragmentAvailableExamsBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.utilities.Utils;
import it.communikein.myunimib.viewmodel.MainActivityViewModel;


/**
 * The {@link Fragment} responsible for showing the user's Available Exams.
 */
public class AvailableExamsFragment extends Fragment implements
        AvailableExamAdapter.ExamClickCallback, LoaderManager.LoaderCallbacks,
        EnrollLoader.EnrollUpdatesListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = AvailableExamsFragment.class.getSimpleName();

    private final static int LOADER_ENROLL_ID = 4001;

    /*  */
    private FragmentAvailableExamsBinding mBinding;

    private Exam chosenExam = null;
    private ProgressDialog progress;

    /* Required empty public constructor */
    public AvailableExamsFragment() {}

    public MainActivityViewModel getViewModel() {
        return ((MainActivity) getActivity()).getViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_available_exams, container, false);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list.
         *
         * The third parameter (reverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        mBinding.rvList.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.rvList.setHasFixedSize(true);

        /* Show data downloading */
        mBinding.swipeRefresh.setOnRefreshListener(this);

        progress = new ProgressDialog(getActivity());
        progress.setCancelable(false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();

        /* Create a new AvailableExamAdapter. It will be responsible for displaying the list's items */
        final AvailableExamAdapter mExamsAdapter = new AvailableExamAdapter(this);

        getViewModel().getAvailableExamsLoading().observe(this, loading -> {
            if (loading != null)
                mBinding.swipeRefresh.setRefreshing(loading);
        });

        getViewModel().getAvailableExams().observe(this, list -> {
            if (list != null) {
                mExamsAdapter.setList((ArrayList<AvailableExam>) list);
            }
        });

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mBinding.rvList.setAdapter(mExamsAdapter);
    }


    @Override
    public void onRefresh() {
        getViewModel().refreshAvailableExams();
    }

    @Override
    public void onListItemClick(ExamID examID) {
        Intent intent = new Intent(getActivity(), AvailableExamDetailActivity.class);
        intent.putExtra(UnimibNetworkDataSource.ADSCE_ID, examID.getAdsceId());
        intent.putExtra(UnimibNetworkDataSource.APP_ID, examID.getAppId());
        intent.putExtra(UnimibNetworkDataSource.ATT_DID_ESA_ID, examID.getAttDidEsaId());
        intent.putExtra(UnimibNetworkDataSource.CDS_ESA_ID, examID.getCdsEsaId());
        startActivity(intent);
    }

    @Override
    public void onEnrollmentClicked(Exam exam) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.attention_title))
                .setMessage(R.string.prompt_enrollment_confirm)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> doEnroll(exam))
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @SuppressWarnings("unchecked")
    private void doEnroll(Exam exam) {
        progress.setMessage(getString(R.string.label_enrollment_in_progress));
        showProgress(true);

        chosenExam = exam;
        getLoaderManager()
                .initLoader(LOADER_ENROLL_ID, null, this)
                .forceLoad();
    }



    @NonNull
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ENROLL_ID:
                if (chosenExam != null) {
                    showProgress(true);

                    return getViewModel().enrollExam(chosenExam, getActivity(), this);
                }
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object result) {
        showProgress(false);

        switch (loader.getId()) {
            case LOADER_ENROLL_ID:
                handleEnrollExam((boolean) result);
                break;

            default:
                break;
        }
    }

    private void handleEnrollExam(boolean enrolled) {
        showProgress(false);

        if (enrolled) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_enrollment_completed)
                    .setMessage(R.string.enrollment_completed_info)
                    .setPositiveButton(R.string.action_go, (dialog, which) -> showCertificate())
                    .setNegativeButton(R.string.action_ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            getViewModel().refreshAvailableExams();
            getViewModel().refreshEnrolledExams();
        }
        else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.attention_title)
                    .setMessage(R.string.enrollment_not_completed)
                    .setNeutralButton(R.string.action_ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {}

    @Override
    public void onEnrollmentUpdate(int status) {
        switch (status) {
            case EnrollLoader.STATUS_STARTED:
                Snackbar.make(mBinding.rvList,
                        R.string.label_enrollment_started, Snackbar.LENGTH_LONG).show();
                break;

            case EnrollLoader.STATUS_ENROLLMENT_OK:
                Snackbar.make(mBinding.rvList,
                        R.string.label_enrollment_confirmed, Snackbar.LENGTH_LONG).show();
                break;

            case EnrollLoader.STATUS_CERTIFICATE_DOWNLOADED:
                Snackbar.make(mBinding.rvList,
                        R.string.label_certificate_downloaded, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open, v -> showCertificate())
                        .show();
                break;

            case EnrollLoader.STATUS_ERROR_QUESTIONNAIRE_TO_FILL:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_questionnaire)
                        .setMessage(R.string.error_questionnaire_to_fill)
                        .setPositiveButton(R.string.action_show_questionnaire,
                                (dialog, which) -> showUnimibWebsite())
                        .setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;

            case EnrollLoader.STATUS_ERROR_CERTIFICATE:
                Snackbar.make(mBinding.rvList,
                        R.string.error_certificate_not_found, Snackbar.LENGTH_LONG).show();
                break;

            case EnrollLoader.STATUS_ERROR_GENERAL:
                Snackbar.make(mBinding.rvList,
                        R.string.error_generic, Snackbar.LENGTH_LONG).show();
                break;

        }
    }



    /**
     * Change the Activity's ActionBar title.
     */
    private void setTitle() {
        if (getActivity() != null) {
        /* Get a reference to the MainActivity ActionBar */
            ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        /* If there is an ActionBar, set it's title */
            if (actionBar != null)
                actionBar.setTitle(R.string.title_exams_available);
        }
    }

    private void showProgress(final boolean show) {
        if (show) progress.show();
        else
        if(progress != null && progress.isShowing()) progress.dismiss();
    }

    private void showUnimibWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.UNIMIB_WEBSITE));
        startActivity(browserIntent);
    }

    private void showCertificate() {
        if (getActivity() != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File certificate_file = chosenExam.getCertificatePath();
            Uri certificate_uri = FileProvider.getUriForFile(getActivity(),
                    getString(R.string.file_provider_authority), certificate_file);

            intent.setDataAndType(certificate_uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }
}
