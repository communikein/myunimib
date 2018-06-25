package it.communikein.myunimib.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.UserHelper.AccountRemovedListener;
import it.communikein.myunimib.data.UserHelper.AccountRemoveErrorListener;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.ProfilePictureVolleyRequest;
import it.communikein.myunimib.data.network.loaders.S3Helper;
import it.communikein.myunimib.ui.graduation.projection.GraduationProjectionFragment;

public class MainActivityViewModel extends ViewModel {

    private final static String TAG = MainActivityViewModel.class.getSimpleName();

    private final UnimibRepository mRepository;
    private final ProfilePictureVolleyRequest mProfilePictureRequest;

    private MutableLiveData<User> mUser;
    //private final LiveData<List<BookletEntry>> mData;

    @Inject
    public MainActivityViewModel(UnimibRepository repository,
                                 ProfilePictureVolleyRequest profilePictureRequest) {
        this.mRepository = repository;
        this.mProfilePictureRequest = profilePictureRequest;
        this.mUser = new MutableLiveData<>();

        mRepository.getUser((user) -> this.mUser.postValue(user));
    }


    public void logout(Activity activity, AccountRemovedListener accountRemovedListener,
                       AccountRemoveErrorListener accountRemoveErrorListener) {
        mRepository.deleteUser(activity, accountRemovedListener, accountRemoveErrorListener);
        mRepository.clearData();
        mProfilePictureRequest.clearCache();
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public void loadProfilePicture(NetworkImageView target) {
        this.mProfilePictureRequest.changeUser(mRepository.getUserAuth());

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



    public LiveData<List<String>> getCoursesNames() {
        return mRepository.getCoursesNames("");
    }

    public void addExamProjection(BookletEntry entry, GraduationProjectionFragment.AddProjectionListener listener) {
        mRepository.addBookletEntry(entry, listener);
    }

    public void deleteExamProjection(BookletEntry entry, GraduationProjectionFragment.DeleteProjectionListener listener) {
        mRepository.deleteBookletEntry(entry, listener);
    }

    public void restoreExamProjection(BookletEntry entry, GraduationProjectionFragment.AddProjectionListener listener) {
        addExamProjection(entry, listener);
    }

}
