package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.ui.exam.booklet.BookletAdapter;
import it.communikein.myunimib.ui.graduation.projection.GraduationProjectionFragment;
import it.communikein.myunimib.ui.timetable.AddLessonActivity;

public class GraduationProjectionViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final LiveData<List<BookletEntry>> mData;
    private MutableLiveData<User> mUser;

    @Inject
    public GraduationProjectionViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = mRepository.getObservableFakeExams();
        mUser = new MutableLiveData<>();
        mRepository.getUser((user -> mUser.postValue(user)));
    }

    public LiveData<List<BookletEntry>> getExams() {
        return mData;
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


    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<List<String>> getCoursesNames() {
        return mRepository.getCoursesNames("");
    }

}
