package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.utilities.DAY_OF_WEEK;

public class TimetableViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final HashMap<String, LiveData<List<Lesson>>> mData;

    @Inject
    public TimetableViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = new HashMap<>();
        for (DAY_OF_WEEK day : DAY_OF_WEEK.values()) {
            mData.put(day.getDay(), mRepository.getObservableTimetable(day.getDay()));
        }
    }

    public User getUser() {
        return mRepository.getUser();
    }

    public LiveData<List<Lesson>> getTimetable(String day) {
        return mData.get(day);
    }

}
