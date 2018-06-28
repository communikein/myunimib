package it.communikein.myunimib.viewmodel.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.network.ProfilePicturePicassoRequest;
import it.communikein.myunimib.data.network.ProfilePictureVolleyRequest;
import it.communikein.myunimib.viewmodel.MainActivityViewModel;

public class MainActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UnimibRepository mRepository;
    private final ProfilePictureVolleyRequest mProfilePictureRequest;
    private final ProfilePicturePicassoRequest mProfilePicturePicassoRequest;

    @Inject
    public MainActivityViewModelFactory(UnimibRepository repository,
                                        ProfilePictureVolleyRequest profilePictureVolleyRequest,
                                        ProfilePicturePicassoRequest profilePicturePicassoRequest) {
        this.mRepository = repository;
        this.mProfilePictureRequest = profilePictureVolleyRequest;
        this.mProfilePicturePicassoRequest = profilePicturePicassoRequest;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MainActivityViewModel(mRepository,
                mProfilePictureRequest, mProfilePicturePicassoRequest);
    }

}
