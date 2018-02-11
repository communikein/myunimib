package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import javax.inject.Inject;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.ProfilePictureVolleyRequest;
import it.communikein.myunimib.data.network.loaders.S3Helper;

public class HomeViewModel extends ViewModel {

    private final UnimibRepository mRepository;
    private final ProfilePictureVolleyRequest mProfilePictureRequest;

    @Inject
    public HomeViewModel(UnimibRepository repository,
                         ProfilePictureVolleyRequest profilePictureRequest) {
        this.mRepository = repository;
        this.mProfilePictureRequest = profilePictureRequest;
    }

    public User getUser() {
        return mRepository.getUser();
    }

    public void loadProfilePicture(NetworkImageView target) {
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
