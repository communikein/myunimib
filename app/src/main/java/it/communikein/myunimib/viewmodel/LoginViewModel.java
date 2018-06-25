package it.communikein.myunimib.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.Faculty;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.loaders.LoginLoader;
import it.communikein.myunimib.data.network.loaders.UserDataLoader;

public class LoginViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private ArrayList<Faculty> mFaculties = new ArrayList<>();
    private Faculty mChosenFaculty = null;

    @Inject
    public LoginViewModel(UnimibRepository repository) {
        this.mRepository = repository;
    }

    public User getUser() {
        return mRepository.getUser(null);
    }

    public void saveUser(User user) {
        mRepository.saveUser(user);
    }

    public String getAccountType() {
        return mRepository.getAccountType();
    }

    public void setFaculties(ArrayList<Faculty> faculties) {
        this.mFaculties = faculties;
    }

    public ArrayList<Faculty> getFaculties() {
        return mFaculties;
    }

    public void setFacultyChosen(Faculty faculty) {
        this.mChosenFaculty = faculty;
    }

    public Faculty getFacultyChosen() {
        return this.mChosenFaculty;
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

    public UserDataLoader downloadUserData(Activity activity) {
        mRepository.updateChosenFaculty(getFacultyChosen());
        return mRepository.updateUserData(activity);
    }

}
