package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.ui.list.timetable.AddLessonActivity;
import it.communikein.myunimib.ui.list.timetable.DayFragment;

public class TimetableViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    @Inject
    public TimetableViewModel(UnimibRepository repository) {
        mRepository = repository;
    }

    public User getUser() {
        return mRepository.getUser();
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

}
