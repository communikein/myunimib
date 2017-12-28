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
    private final LiveData<Boolean> mLoading;

    public AvailableExamsListViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = repository.getCurrentAvailableExams();
        mChanges = repository.getModifiedAvailableExamsCount();
        mLoading = repository.getAvailableExamsLoading();
    }

    public LiveData<List<AvailableExam>> getAvailableExams() {
        return mData;
    }

    public LiveData<Integer> getModifiedAvailableExamsCount() {
        return mChanges;
    }

    public LiveData<Boolean> getAvailableExamsLoading() {
        return mLoading;
    }

    public void clearChanges() {
        mChanges.setValue(0);
    }

    public void refreshAvailableExams() {
        mRepository.startFetchAvailableExamsService();
    }

}
