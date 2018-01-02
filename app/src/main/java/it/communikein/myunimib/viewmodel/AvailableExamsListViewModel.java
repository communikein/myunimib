package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.AvailableExam;


public class AvailableExamsListViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final LiveData<List<AvailableExam>> mData;
    private final LiveData<Boolean> mLoading;

    @Inject
    public AvailableExamsListViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = repository.getObservableCurrentAvailableExams();
        mLoading = repository.getAvailableExamsLoading();
    }

    public LiveData<List<AvailableExam>> getAvailableExams() {
        return mData;
    }

    public LiveData<Boolean> getAvailableExamsLoading() {
        return mLoading;
    }

    public UnimibRepository getRepository() {
        return mRepository;
    }


    public void refreshAvailableExams() {
        mRepository.startFetchAvailableExamsService();
    }

    public void refreshEnrolledExams() {
        mRepository.startFetchEnrolledExamsService();
    }

}
