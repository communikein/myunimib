package it.communikein.myunimib.ui.list.timetable;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.databinding.FragmentDayBinding;
import it.communikein.myunimib.utilities.DAY_OF_WEEK;
import it.communikein.myunimib.viewmodel.TimetableViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class DayFragment extends Fragment {

    private static final String LOG_TAG = DayFragment.class.getSimpleName();

    private FragmentDayBinding mBinding;

    private DAY_OF_WEEK day_of_week;

    public DayFragment() { }

    void setDay(DAY_OF_WEEK day_of_week) {
        this.day_of_week = day_of_week;
    }

    public DAY_OF_WEEK getDay() {
        return day_of_week;
    }

    public static DayFragment create(DAY_OF_WEEK day_of_week) {
        DayFragment dayFragment = new DayFragment();
        dayFragment.setDay(day_of_week);

        return dayFragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_day, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        mBinding.listRecyclerview.setLayoutManager(layoutManager);

        /*
         * Improve performance if sure that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.listRecyclerview.setHasFixedSize(true);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
         * Ensures a loader is initialized and active and shows the loading view.
         * If the loader doesn't already exist, one is created and (if the activity/fragment is
         * currently started) starts the loader. Otherwise the last created loader is re-used.
         */
        if (getParentViewModel() != null && !getParentViewModel().getUser().isFake()) {

            LessonsListAdapter mAdapter = new LessonsListAdapter();

            getParentViewModel().getTimetable(day_of_week.getDay()).observe(this, list -> {
                if (list != null) {
                    Log.d(LOG_TAG, "Updating the booklet list. " + list.size() + " elements.");
                    mAdapter.setList((ArrayList<Lesson>) list);
                }
            });

            /* Setting the adapter attaches it to the RecyclerView in our layout. */
            mBinding.listRecyclerview.setAdapter(mAdapter);
        }
    }

    private TimetableViewModel getParentViewModel() {
        if (getParentFragment() != null)
            return ((TimetableFragment) getParentFragment()).getViewModel();
        else
            return null;
    }

}
