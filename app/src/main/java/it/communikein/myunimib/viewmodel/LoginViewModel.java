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

    private String mTempUsername = "";
    private String mTempPassword = "";
    private String mTempName = "";

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

    public void deleteUser() {
        mRepository.deleteUser(null, null, null);
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


    public void setTempUsername(String username) {
        this.mTempUsername = username;
    }

    public void setTempPassword(String password) {
        this.mTempPassword = password;
    }

    public void setTempName(String name) {
        this.mTempName = name;
    }

    public String getTempUsername() {
        return this.mTempUsername;
    }

    public String getTempPassword() {
        return this.mTempPassword;
    }

    public String getTempName() {
        return this.mTempName;
    }



    public LoginLoader doLogin(Activity activity, String username, String password, String name, boolean fake) {
        User temp_user = new User(username, password);
        temp_user.setRealName(name);
        temp_user.setFake(fake);

        return mRepository.loginUser(temp_user, activity);
    }

    public LoginLoader doLogin(Activity activity, String username, String password) {
        return doLogin(activity, username, password, "", false);
    }

    public LoginLoader doFakeLogin(Activity activity, String username, String password, String name) {
        return doLogin(activity, username, password, name, true);
    }

    public UserDataLoader downloadUserData(Activity activity) {
        mRepository.updateChosenFaculty(getFacultyChosen());
        return mRepository.updateUserData(activity);
    }

}
