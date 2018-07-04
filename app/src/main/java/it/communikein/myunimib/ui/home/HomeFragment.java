package it.communikein.myunimib.ui.home;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.communikein.myunimib.R;
import it.communikein.myunimib.databinding.FragmentHomeBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.utilities.Utils;
import it.communikein.myunimib.viewmodel.MainActivityViewModel;


/**
 * A simple {@link Fragment} subclass.
 */

public class HomeFragment extends Fragment {

    public static final String TAG = HomeFragment.class.getName();

    /*  */
    private FragmentHomeBinding mBinding;

    /* Required empty public constructor */
    public HomeFragment() {}

    private MainActivityViewModel getViewModel() {
        return ((MainActivity) getActivity()).getViewModel();
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

        setTitle();
        hideTabs();
        showBottomNavigation();

        updateUI();

        getViewModel().loadProfilePictureVolley(mBinding.userImageView);
        /*
        getViewModel().loadProfilePicturePicasso(new ProfilePicturePicassoRequest.ImageDownloadCallback() {
            @Override
            public void onImageReady(Bitmap bitmap) {
                mBinding.userImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onImageError(Exception e) {

            }
        });
        */
    }

    private void updateUI() {
        getViewModel().getUser().observe(this, (user) -> {
            if (user != null) {
                mBinding.userNameTextView.setText(user.getRealName());
                mBinding.matricolaTextView.setText(user.getMatricola());
                mBinding.averageMarkTextView.setText(user.printAverageScore());
                mBinding.cfuTextView.setText(user.printTotalCfu());
            }
        });
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
