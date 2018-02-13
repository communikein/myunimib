package it.communikein.myunimib.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.loaders.LoginLoader;
import it.communikein.myunimib.data.network.loaders.UserDataLoader;

public class LoginViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    @Inject
    public LoginViewModel(UnimibRepository repository) {
        this.mRepository = repository;
    }

    public User getUser() {
        return mRepository.getUser();
    }

    public String getAccountType() {
        return mRepository.getAccountType();
    }

    public LoginLoader doLogin(Activity activity, String username, String password) {
        User temp_user = new User(username, password);
        temp_user.setFake(false);

        return mRepository.loginUser(temp_user, activity);
    }

    public LoginLoader doFakeLogin(Activity activity) {
        User temp_user = new User("fake", "fake");
        temp_user.setFake(true);

        return mRepository.loginUser(temp_user, activity);
    }

    public UserDataLoader downloadUserData(Activity activity, int selectedFaculty) {
        mRepository.updateChosenFaculty(selectedFaculty);
        return mRepository.updateUserData(activity);
    }

}
