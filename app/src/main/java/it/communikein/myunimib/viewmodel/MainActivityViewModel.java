package it.communikein.myunimib.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import javax.inject.Inject;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.UserHelper.AccountRemovedListener;
import it.communikein.myunimib.data.UserHelper.AccountRemoveErrorListener;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.ProfilePictureVolleyRequest;
import it.communikein.myunimib.data.network.loaders.S3Helper;

public class MainActivityViewModel extends ViewModel {

    private final static String TAG = MainActivityViewModel.class.getSimpleName();

    private final UnimibRepository mRepository;
    private final ProfilePictureVolleyRequest mProfilePictureRequest;

    @Inject
    public MainActivityViewModel(UnimibRepository repository,
                                 ProfilePictureVolleyRequest profilePictureRequest) {
        this.mRepository = repository;
        this.mProfilePictureRequest = profilePictureRequest;
    }


    public void logout(Activity activity, AccountRemovedListener accountRemovedListener,
                       AccountRemoveErrorListener accountRemoveErrorListener) {
        mRepository.deleteUser(activity, accountRemovedListener, accountRemoveErrorListener);
        mRepository.clearData();
        mProfilePictureRequest.clearCache();
    }

    public User getUser() {
        return mRepository.getUser();
    }

    public void loadProfilePicture(NetworkImageView target) {
        this.mProfilePictureRequest.changeUser(getUser());

        ProfilePictureVolleyRequest.ProfilePictureLoader imageLoader =
                mProfilePictureRequest.getImageLoader();

        imageLoader.get(S3Helper.URL_PROFILE_PICTURE,
                ImageLoader.getImageListener(
                        target,
                        R.drawable.ic_person_black_24dp,
                        android.R.drawable.ic_dialog_alert)
        );

        target.setImageUrl(S3Helper.URL_PROFILE_PICTURE, imageLoader);
    }

}
