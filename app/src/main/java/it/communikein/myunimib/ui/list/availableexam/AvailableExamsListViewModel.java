package it.communikein.myunimib.ui.list.availableexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.AvailableExam;


class AvailableExamsListViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final LiveData<List<AvailableExam>> mData;
    private final MutableLiveData<Integer> mChanges;

    public AvailableExamsListViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = repository.getCurrentAvailableExams();
        mChanges = repository.getModifiedAvailableExamsCount();
    }

    public LiveData<List<AvailableExam>> getAvailableExams() {
        return mData;
    }

    public LiveData<Integer> getModifiedAvailableExamsCount() {
        return mChanges;
    }

    public void clearChanges() {
        mChanges.setValue(0);
    }

    public void refreshAvailableExams() {
        mRepository.startFetchAvailableExamsService();
    }

}
