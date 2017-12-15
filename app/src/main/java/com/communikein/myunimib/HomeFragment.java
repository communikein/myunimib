package com.communikein.myunimib;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.communikein.myunimib.databinding.FragmentHomeBinding;
import com.communikein.myunimib.sync.ProfilePictureVolleyRequest;
import com.communikein.myunimib.sync.S3Helper;
import com.communikein.myunimib.utilities.UserUtils;
import com.communikein.myunimib.utilities.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    FragmentHomeBinding mBinding;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        if (Utils.user == null)
            Utils.user = UserUtils.getUser(getActivity());

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.userNameTextView.setText(Utils.user.getName());
        mBinding.matricolaTextView.setText(Utils.user.getMatricola());
        mBinding.averageMarkTextView.setText(Utils.markFormat.format(Utils.user.getAverageMark()));
        mBinding.cfuTextView.setText(String.valueOf(Utils.user.getTotalCFU()));

        loadProfilePicture();
    }

    private void loadProfilePicture(){
        ProfilePictureVolleyRequest profilePictureVolleyRequest =
                new ProfilePictureVolleyRequest(getActivity(), Utils.user);
        ImageLoader imageLoader = profilePictureVolleyRequest.getImageLoader();

        imageLoader.get(S3Helper.URL_PROFILE_PICTURE,
                ImageLoader.getImageListener(
                        mBinding.userImageView,
                        R.drawable.ic_person_black_24dp,
                        android.R.drawable.ic_dialog_alert)
        );
        mBinding.userImageView.setImageUrl(S3Helper.URL_PROFILE_PICTURE, imageLoader);
    }
}
