package it.communikein.myunimib.ui.list.timetable;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import it.communikein.myunimib.R;
import it.communikein.myunimib.databinding.FragmentTimetableBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.utilities.DAY_OF_WEEK;
import it.communikein.myunimib.viewmodel.TimetableViewModel;
import it.communikein.myunimib.viewmodel.factory.BookletViewModelFactory;
import it.communikein.myunimib.viewmodel.factory.TimetableViewModelFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimetableFragment extends Fragment {

    private static final String LOG_TAG = TimetableFragment.class.getSimpleName();

    public final ArrayList<String> TABS_TITLE = new ArrayList<>();
    private String FRAGMENT_MONDAY;
    private String FRAGMENT_TUESDAY;
    private String FRAGMENT_WEDNESDAY;
    private String FRAGMENT_THURSDAY;
    private String FRAGMENT_FRIDAY;
    private String FRAGMENT_SATURDAY;
    private String FRAGMENT_SUNDAY;

    /*  */
    private FragmentTimetableBinding mBinding;

    /* */
    @Inject
    TimetableViewModelFactory viewModelFactory;

    /* */
    private TimetableViewModel mViewModel;

    /* Required empty public constructor */
    public TimetableFragment() {
        TABS_TITLE.add(FRAGMENT_MONDAY);
        TABS_TITLE.add(FRAGMENT_TUESDAY);
        TABS_TITLE.add(FRAGMENT_WEDNESDAY);
        TABS_TITLE.add(FRAGMENT_THURSDAY);
        TABS_TITLE.add(FRAGMENT_FRIDAY);
        TABS_TITLE.add(FRAGMENT_SATURDAY);
        TABS_TITLE.add(FRAGMENT_SUNDAY);
    }


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);

        FRAGMENT_MONDAY = getString(R.string.monday);
        FRAGMENT_TUESDAY = getString(R.string.tuesday);
        FRAGMENT_WEDNESDAY = getString(R.string.wednesday);
        FRAGMENT_THURSDAY = getString(R.string.thursday);
        FRAGMENT_FRIDAY = getString(R.string.friday);
        FRAGMENT_SATURDAY = getString(R.string.saturday);
        FRAGMENT_SUNDAY = getString(R.string.sunday);
    }

    public TimetableViewModel getViewModel() {
        return mViewModel;
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return mBinding.coordinatorLayout;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_timetable, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(TimetableViewModel.class);

        showTabs();
        hideBottomNavigation();
        initFab();

        initViewPager();

        showCurrentDay();
    }

    private void showCurrentDay() {
        Calendar currentTime = Calendar.getInstance();
        int day = currentTime.get(Calendar.DAY_OF_WEEK) - 2;
        if (day == -1)
            day = 6;

        mBinding.viewpager.setCurrentItem(day);
    }

    private void setTitle() {
        if (getActivity() != null) {
            /* Get a reference to the MainActivity ActionBar */
            ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
            /* If there is an ActionBar, set it's title */
            if (actionBar != null)
                actionBar.setTitle(R.string.title_timetable);
        }
    }

    private void initViewPager() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            initViewPager(mBinding.viewpager);
            ((MainActivity) getActivity()).getTabsLayout().setupWithViewPager(mBinding.viewpager);
        }
    }


    private void initViewPager(ViewPager viewPager) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            DaysPagerAdapter adapter = new DaysPagerAdapter(getChildFragmentManager());

            adapter.addFragment(DayFragment.create(DAY_OF_WEEK.MONDAY), FRAGMENT_MONDAY);
            adapter.addFragment(DayFragment.create(DAY_OF_WEEK.TUESDAY), FRAGMENT_TUESDAY);
            adapter.addFragment(DayFragment.create(DAY_OF_WEEK.WEDNESDAY), FRAGMENT_WEDNESDAY);
            adapter.addFragment(DayFragment.create(DAY_OF_WEEK.THURSDAY), FRAGMENT_THURSDAY);
            adapter.addFragment(DayFragment.create(DAY_OF_WEEK.FRIDAY), FRAGMENT_FRIDAY);
            adapter.addFragment(DayFragment.create(DAY_OF_WEEK.SATURDAY), FRAGMENT_SATURDAY);
            adapter.addFragment(DayFragment.create(DAY_OF_WEEK.SUNDAY), FRAGMENT_SUNDAY);

            viewPager.setAdapter(adapter);
        }
    }

    private void initFab() {
        mBinding.fab.setOnClickListener(v -> {
            int selected = mBinding.viewpager.getCurrentItem();
            DaysPagerAdapter adapter = (DaysPagerAdapter) mBinding.viewpager.getAdapter();

            if (adapter != null) {
                DayFragment fragment = adapter.getCurrent(selected);
                Intent intent = new Intent(getActivity(), AddLessonActivity.class);
                intent.putExtra(AddLessonActivity.DAY, fragment.getDay());
                startActivity(intent);
            }
        });
    }

    private void showTabs() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).getTabLayout().setTabGravity(TabLayout.GRAVITY_CENTER);
            ((MainActivity) getActivity()).getTabLayout().setTabMode(TabLayout.MODE_SCROLLABLE);
            ((MainActivity) getActivity()).showTabsLayout(TABS_TITLE);
        }
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).hideBottomNavigation();
        }
    }


    static class DaysPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        DaysPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public DayFragment getCurrent(int position) {
            return (DayFragment) getItem(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
