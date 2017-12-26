package it.communikein.myunimib.ui.list.enrolledexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.EnrolledExam;


class EnrolledExamsListViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final LiveData<List<EnrolledExam>> mData;
    private final MutableLiveData<Integer> mChanges;

    public EnrolledExamsListViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = mRepository.getCurrentEnrolledExams();
        mChanges = mRepository.getModifiedEnrolledExamsCount();
    }

    public LiveData<List<EnrolledExam>> getEnrolledExams() {
        return mData;
    }

    public LiveData<Integer> getModifiedEnrolledExamsCount() {
        return mChanges;
    }

    public void clearChanges() {
        mChanges.setValue(0);
    }

    public void refreshEnrolledExams() {
        mRepository.startFetchEnrolledExamsService();
    }

}
