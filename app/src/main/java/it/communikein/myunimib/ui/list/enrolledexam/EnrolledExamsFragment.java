package it.communikein.myunimib.ui.list.enrolledexam;


import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.EnrolledExam;
import it.communikein.myunimib.data.database.ExamID;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.databinding.FragmentExamsBinding;
import it.communikein.myunimib.di.Injectable;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.ui.detail.EnrolledExamDetailActivity;
import it.communikein.myunimib.utilities.UserUtils;
import it.communikein.myunimib.viewmodel.EnrolledExamsListViewModel;
import it.communikein.myunimib.viewmodel.factory.AvailableExamsViewModelFactory;
import it.communikein.myunimib.viewmodel.factory.EnrolledExamsViewModelFactory;


/**
 * The {@link Fragment} responsible for showing the user's Enrolled Exams.
 */
public class EnrolledExamsFragment extends Fragment implements
        EnrolledExamAdapter.ExamClickCallback, SwipeRefreshLayout.OnRefreshListener,
        Injectable {

    private static final String LOG_TAG = EnrolledExamsFragment.class.getSimpleName();

    /*  */
    private FragmentExamsBinding mBinding;

    /* */
    @Inject
    EnrolledExamsViewModelFactory viewModelFactory;

    /* */
    private EnrolledExamsListViewModel mViewModel;

    /* Required empty public constructor */
    public EnrolledExamsFragment() {}


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_exams, container, false);

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

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();

        /*
         * Ensures a loader is initialized and active and shows the loading view.
         * If the loader doesn't already exist, one is created and (if the activity/fragment is
         * currently started) starts the loader. Otherwise the last created loader is re-used.
         */
        if (getActivity() != null && !UserUtils.getUser(getActivity()).isFake()) {
            /* Create a new EnrolledExamAdapter. It will be responsible for displaying the list's items */
            final EnrolledExamAdapter mExamsAdapter = new EnrolledExamAdapter(this);

            mViewModel = ViewModelProviders
                    .of(this, viewModelFactory)
                    .get(EnrolledExamsListViewModel.class);

            mViewModel.getEnrolledExamsLoading().observe(this, loading -> {
                if (loading != null)
                    mBinding.swipeRefresh.setRefreshing(loading);
            });

            mViewModel.getEnrolledExams().observe(this, list -> {
                if (list != null) {
                    Log.d(LOG_TAG, "Updating the enrolled exams list. " + list.size() + " elements.");
                    mExamsAdapter.setList((ArrayList<EnrolledExam>) list);
                }
            });

            /* Setting the adapter attaches it to the RecyclerView in our layout. */
            mBinding.rvList.setAdapter(mExamsAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                onRefresh();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRefresh() {
        mViewModel.refreshEnrolledExams();
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
                actionBar.setTitle(R.string.title_exams_enrolled);
        }
    }


    @Override
    public void onListItemClick(ExamID examID) {
        Intent intent = new Intent(getActivity(), EnrolledExamDetailActivity.class);
        intent.putExtra(UnimibNetworkDataSource.ADSCE_ID, examID.getAdsceId());
        intent.putExtra(UnimibNetworkDataSource.APP_ID, examID.getAppId());
        intent.putExtra(UnimibNetworkDataSource.ATT_DID_ESA_ID, examID.getAttDidEsaId());
        intent.putExtra(UnimibNetworkDataSource.CDS_ESA_ID, examID.getCdsEsaId());
        startActivity(intent);
    }
}
