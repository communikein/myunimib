package it.communikein.myunimib.viewmodel.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.network.ProfilePictureVolleyRequest;
import it.communikein.myunimib.viewmodel.HomeViewModel;
import it.communikein.myunimib.viewmodel.LoginViewModel;

public class HomeViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UnimibRepository mRepository;
    private final ProfilePictureVolleyRequest mProfilePictureRequest;

    @Inject
    public HomeViewModelFactory(UnimibRepository repository,
                                ProfilePictureVolleyRequest profilePictureVolleyRequest) {
        this.mRepository = repository;
        this.mProfilePictureRequest = profilePictureVolleyRequest;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new HomeViewModel(mRepository, mProfilePictureRequest);
    }

}
