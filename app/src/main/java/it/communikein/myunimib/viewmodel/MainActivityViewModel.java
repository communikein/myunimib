package it.communikein.myunimib.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.UserHelper.AccountRemovedListener;
import it.communikein.myunimib.data.UserHelper.AccountRemoveErrorListener;
import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.Exam;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.ProfilePicturePicassoRequest;
import it.communikein.myunimib.data.network.ProfilePictureVolleyRequest;
import it.communikein.myunimib.data.network.loaders.EnrollLoader;
import it.communikein.myunimib.data.network.loaders.S3Helper;
import it.communikein.myunimib.ui.graduation.projection.GraduationProjectionFragment;
import it.communikein.myunimib.ui.timetable.AddLessonActivity;
import it.communikein.myunimib.ui.timetable.DayFragment;

public class MainActivityViewModel extends ViewModel {

    private final static String TAG = MainActivityViewModel.class.getSimpleName();

    private final UnimibRepository mRepository;

    private final ProfilePictureVolleyRequest mProfilePictureRequest;
    private final ProfilePicturePicassoRequest mProfilePicturePicassoRequest;

    private MutableLiveData<User> mUser;
    private MutableLiveData<Building> mSelectedBuilding;
    private final List<Building> mBuildings;

    private final LiveData<List<BookletEntry>> mFakeExams;
    private final LiveData<List<BookletEntry>> mBooklet;
    private final LiveData<List<EnrolledExam>> mEnrolledExams;
    private final LiveData<List<AvailableExam>> mAvailableExams;

    private final LiveData<Boolean> mBookletLoading;
    private final LiveData<Boolean> mEnrolledExamsLoading;
    private final LiveData<Boolean> mAvailableExamsLoading;

    @Inject
    public MainActivityViewModel(UnimibRepository repository,
                                 ProfilePictureVolleyRequest profilePictureRequest,
                                 ProfilePicturePicassoRequest profilePicturePicassoRequest) {
        this.mRepository = repository;
        this.mProfilePictureRequest = profilePictureRequest;
        this.mProfilePicturePicassoRequest = profilePicturePicassoRequest;

        this.mUser = new MutableLiveData<>();
        this.mSelectedBuilding = new MutableLiveData<>();

        this.mBooklet = mRepository.getObservableCurrentBooklet();
        this.mEnrolledExams = mRepository.getObservableCurrentEnrolledExams();
        this.mAvailableExams = mRepository.getObservableCurrentAvailableExams();

        this.mBookletLoading = mRepository.getBookletLoading();
        this.mEnrolledExamsLoading = mRepository.getEnrolledExamsLoading();
        this.mAvailableExamsLoading = mRepository.getAvailableExamsLoading();

        mRepository.getUser((user) -> this.mUser.postValue(user));
        this.mFakeExams = mRepository.getObservableFakeExams();
        this.mBuildings = mRepository.getCurrentBuildings();
    }

    public UnimibRepository getRepository() {
        return mRepository;
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

    public void loadProfilePictureVolley(NetworkImageView target) {
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

    public void loadProfilePicturePicasso(ProfilePicturePicassoRequest.ImageDownloadCallback callback) {
        this.mProfilePicturePicassoRequest.changeUser(mRepository.getUserAuth());

        this.mProfilePicturePicassoRequest.setImageDownloadCallback(callback);
        this.mProfilePicturePicassoRequest.displayProfilePicture();
    }

    public void loadProfilePicturePicassoTwo(Context app, ImageView target) {
        this.mProfilePicturePicassoRequest.changeUser(mRepository.getUserAuth());

        this.mProfilePicturePicassoRequest.displayProfilePicturePicasso(app, target);
    }



    public LiveData<List<BookletEntry>> getFakeExams() {
        return mFakeExams;
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



    public LiveData<List<Lesson>> getTimetable(String day) {
        return mRepository.getObservableTimetable(day);
    }

    public void deleteLesson(Lesson lesson, DayFragment.DeleteLessonListener listener) {
        mRepository.deleteLesson(lesson.getId(), listener);
    }

    public void restoreLesson(Lesson lesson, AddLessonActivity.AddLessonListener listener) {
        mRepository.addLesson(lesson, listener);
    }



    public List<Building> getBuildings() {
        return mBuildings;
    }

    public LiveData<Building> getSelectedBuilding() {
        return mSelectedBuilding;
    }

    public void setSelectedBuilding(Building building) {
        this.mSelectedBuilding.postValue(building);
    }



    public LiveData<List<BookletEntry>> getBooklet() {
        return mBooklet;
    }

    public LiveData<Boolean> getBookletLoading() {
        return mBookletLoading;
    }

    public void refreshBooklet() {
        mRepository.fetchBooklet();
    }



    public LiveData<List<EnrolledExam>> getEnrolledExams() {
        return mEnrolledExams;
    }

    public LiveData<Boolean> getEnrolledExamsLoading() {
        return mEnrolledExamsLoading;
    }

    public void refreshEnrolledExams() {
        mRepository.fetchEnrolledExams();
    }



    public LiveData<List<AvailableExam>> getAvailableExams() {
        return mAvailableExams;
    }

    public LiveData<Boolean> getAvailableExamsLoading() {
        return mAvailableExamsLoading;
    }

    public void refreshAvailableExams() {
        mRepository.fetchAvailableExams();
    }

    public EnrollLoader enrollExam(Exam exam, Activity activity,
                                   EnrollLoader.EnrollUpdatesListener enrollUpdatesListener) {
        return mRepository.enrollExam(exam, activity, enrollUpdatesListener);
    }

}
