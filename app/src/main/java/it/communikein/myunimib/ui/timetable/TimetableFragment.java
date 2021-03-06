package it.communikein.myunimib.ui.timetable;


import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.communikein.myunimib.R;
import it.communikein.myunimib.databinding.FragmentTimetableBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.utilities.DAY_OF_WEEK;
import it.communikein.myunimib.viewmodel.MainActivityViewModel;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimetableFragment extends Fragment {

    public static final String TAG = TimetableFragment.class.getSimpleName();

    public static final int CODE_CREATE_LESSON = 254;

    private final ArrayList<String> TABS_TITLE = new ArrayList<>();
    private String FRAGMENT_MONDAY;
    private String FRAGMENT_TUESDAY;
    private String FRAGMENT_WEDNESDAY;
    private String FRAGMENT_THURSDAY;
    private String FRAGMENT_FRIDAY;
    private String FRAGMENT_SATURDAY;
    private String FRAGMENT_SUNDAY;

    /*  */
    private FragmentTimetableBinding mBinding;

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
        super.onAttach(context);

        FRAGMENT_MONDAY = getString(R.string.monday);
        FRAGMENT_TUESDAY = getString(R.string.tuesday);
        FRAGMENT_WEDNESDAY = getString(R.string.wednesday);
        FRAGMENT_THURSDAY = getString(R.string.thursday);
        FRAGMENT_FRIDAY = getString(R.string.friday);
        FRAGMENT_SATURDAY = getString(R.string.saturday);
        FRAGMENT_SUNDAY = getString(R.string.sunday);
    }

    public MainActivityViewModel getViewModel() {
        return ((MainActivity) getActivity()).getViewModel();
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

        showTabs();
        hideBottomNavigation();
        initFab();

        initViewPager();
        mBinding.viewpager.setCurrentItem(getCurrentDay());
    }

    private int getCurrentDay() {
        Calendar currentTime = Calendar.getInstance();
        int day = currentTime.get(Calendar.DAY_OF_WEEK) - 2;
        if (day == -1)
            day = 6;

        return day;
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
                startActivityForResult(intent, CODE_CREATE_LESSON);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CODE_CREATE_LESSON:

                switch (resultCode) {
                    case RESULT_OK:
                        int selected = data.getIntExtra(AddLessonActivity.DAY, getCurrentDay());
                        mBinding.viewpager.setCurrentItem(selected);

                        Snackbar.make(mBinding.coordinatorLayout,
                                R.string.lesson_added, Snackbar.LENGTH_SHORT).show();
                        break;
                    case RESULT_CANCELED:
                        Snackbar.make(mBinding.coordinatorLayout,
                                R.string.action_discarded, Snackbar.LENGTH_SHORT).show();
                        break;
                }

                break;
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

        DayFragment getCurrent(int position) {
            return (DayFragment) getItem(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
