package it.communikein.myunimib.ui.detail;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.databinding.FragmentHomeBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.utilities.Utils;
import it.communikein.myunimib.viewmodel.BookletViewModel;
import it.communikein.myunimib.viewmodel.HomeViewModel;
import it.communikein.myunimib.viewmodel.factory.HomeViewModelFactory;


/**
 * A simple {@link Fragment} subclass.
 */

public class HomeFragment extends Fragment {

    /*  */
    private FragmentHomeBinding mBinding;

    /* */
    @Inject
    HomeViewModelFactory viewModelFactory;

    /* */
    private HomeViewModel mViewModel;

    /* Required empty public constructor */
    public HomeFragment() {}


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(HomeViewModel.class);

        setTitle();
        hideTabs();
        showBottomNavigation();

        User user = mViewModel.getUser();
        mBinding.userNameTextView.setText(user.getName());
        mBinding.matricolaTextView.setText(user.getMatricola());
        mBinding.averageMarkTextView.setText(Utils.markFormat.format(user.getAverageMark()));
        mBinding.cfuTextView.setText(String.valueOf(user.getTotalCFU()));

        mViewModel.loadProfilePicture(mBinding.userImageView);
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
                actionBar.setTitle(R.string.title_home);
        }
    }

    private void hideTabs() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).hideTabsLayout();
        }
    }

    private void showBottomNavigation() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showBottomNavigation();
        }
    }
}
