package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.ui.list.timetable.AddLessonActivity;
import it.communikein.myunimib.utilities.DAY_OF_WEEK;

public class AddLessonViewModel extends ViewModel {

    private final static String TAG = AddLessonViewModel.class.getSimpleName();

    private final UnimibRepository mRepository;

    private DAY_OF_WEEK dayOfWeek = null;

    @Inject
    public AddLessonViewModel(UnimibRepository repository) {
        this.mRepository = repository;
    }

    public boolean setDayOfWeek(DAY_OF_WEEK dayOfWeek) {
        if (this.dayOfWeek == null) {
            this.dayOfWeek = dayOfWeek;
            return true;
        }
        return false;
    }

    public String getDay() {
        return dayOfWeek.getDay();
    }

    public LiveData<Lesson> getLesson(int lessonId) {
        return mRepository.getObservableLesson(lessonId);
    }

    public void addLesson(Lesson lesson, AddLessonActivity.AddLessonListener listener) {
        mRepository.addLesson(lesson, listener);
    }

    public LiveData<List<String>> getCoursesNames(String like) {
        return mRepository.getCoursesNames(like);
    }

    public ArrayList<String> getBuildings(String like) {
        ArrayList<String> buildings = new ArrayList<>();

        for (Building b : mRepository.getCurrentBuildings())
            if (b.getName().contains(like.toUpperCase()))
                buildings.add(b.getName());

        return buildings;
    }
}
