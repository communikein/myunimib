package it.communikein.myunimib.ui.enrolledexam;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.databinding.FragmentExamsBinding;
import it.communikein.myunimib.ui.EnrolledExamDetailsActivity;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.utilities.InjectorUtils;
import it.communikein.myunimib.utilities.UserUtils;


/**
 * The {@link Fragment} responsible for showing the user's Enrolled Exams.
 */
public class EnrolledExamsFragment extends Fragment implements
        EnrolledExamAdapter.ListItemClickListener {

    /*  */
    private EnrolledExamAdapter mExamsAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    /*  */
    private FragmentExamsBinding mBinding;

    /* */
    private EnrolledExamsFragmentViewModel mViewModel;

    /* Required empty public constructor */
    public EnrolledExamsFragment() {}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

        /* Create a new EnrolledExamAdapter. It will be responsible for displaying the list's items */
        if (getActivity() != null)
            mExamsAdapter = new EnrolledExamAdapter(getActivity(), this);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mBinding.rvList.setAdapter(mExamsAdapter);

        /*
         * Ensures a loader is initialized and active and shows the loading view.
         * If the loader doesn't already exist, one is created and (if the activity/fragment is
         * currently started) starts the loader. Otherwise the last created loader is re-used.
         */
        if (!UserUtils.getUser(getActivity()).isFake() && getActivity() != null) {
            EnrolledExamsViewModelFactory factory = InjectorUtils
                    .provideEnrolledExamsViewModelFactory(this.getContext());
            mViewModel = ViewModelProviders.of(this, factory)
                    .get(EnrolledExamsFragmentViewModel.class);

            mViewModel.getEnrolledExams().observe(this, newData -> {
                mExamsAdapter.swapData(newData);
                if (mPosition == RecyclerView.NO_POSITION)
                    mPosition = 0;

                mBinding.rvList.smoothScrollToPosition(mPosition);
                /*
                if (newData != null && newData.size() != 0) showWeatherDataView();
                else showLoading();
                */
            });
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
                actionBar.setTitle(R.string.title_exams_enrolled);
        }
    }


    @Override
    public void onListItemClick(int exam_adsce_id) {
        Intent intent = new Intent(getActivity(), EnrolledExamDetailsActivity.class);
        intent.putExtra(UnimibNetworkDataSource.ADSCE_ID, exam_adsce_id);
        startActivity(intent);
    }
}
