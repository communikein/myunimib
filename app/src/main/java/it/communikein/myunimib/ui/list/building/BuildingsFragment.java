package it.communikein.myunimib.ui.list.building;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import it.communikein.myunimib.R;
import it.communikein.myunimib.databinding.FragmentBuildingsBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.viewmodel.BuildingsViewModel;
import it.communikein.myunimib.viewmodel.factory.BuildingsViewModelFactory;


public class BuildingsFragment extends Fragment {

    private static final String TAG = BuildingsFragment.class.getSimpleName();

    public static final ArrayList<String> TABS_TITLE = new ArrayList<>();
    private static final String FRAGMENT_MAP_TITLE = "Map";
    private static final String FRAGMENT_LIST_TITLE = "List";

    /* */
    private FragmentBuildingsBinding mBinding;

    /* */
    @Inject
    BuildingsViewModelFactory viewModelFactory;

    /* */
    private BuildingsViewModel mViewModel;

    public BuildingsFragment() {
        TABS_TITLE.add(FRAGMENT_MAP_TITLE);
        TABS_TITLE.add(FRAGMENT_LIST_TITLE);
    }


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    public BuildingsViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding =  DataBindingUtil.inflate(inflater, R.layout.fragment_buildings, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(BuildingsViewModel.class);

        showTabs();
        hideBottomNavigation();

        initViewPager();
    }

    private void initViewPager() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            initViewPager(mBinding.viewpager);
            ((MainActivity) getActivity()).getTabsLayout().setupWithViewPager(mBinding.viewpager);
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
                actionBar.setTitle(R.string.title_buildings);
        }
    }


    private void initViewPager(ViewPager viewPager) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            BuildingsPagerAdapter adapter = new BuildingsPagerAdapter(getChildFragmentManager());

            BuildingsMapFragment buildingsMapFragment = new BuildingsMapFragment();
            BuildingsListFragment buildingsListFragment = new BuildingsListFragment();

            adapter.addFragment(buildingsMapFragment, FRAGMENT_MAP_TITLE);
            adapter.addFragment(buildingsListFragment, FRAGMENT_LIST_TITLE);

            viewPager.setAdapter(adapter);
        }
    }

    private void showTabs() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).getTabLayout().setTabGravity(TabLayout.GRAVITY_FILL);
            ((MainActivity) getActivity()).getTabLayout().setTabMode(TabLayout.MODE_FIXED);
            ((MainActivity) getActivity()).showTabsLayout(TABS_TITLE);
        }
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).hideBottomNavigation();
        }
    }


    static class BuildingsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        BuildingsPagerAdapter(FragmentManager manager) {
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

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
