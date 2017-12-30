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
    private final LiveData<Boolean> mLoading;

    public EnrolledExamsListViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = mRepository.getObservableCurrentEnrolledExams();
        mLoading = mRepository.getEnrolledExamsLoading();
    }

    public LiveData<List<EnrolledExam>> getEnrolledExams() {
        return mData;
    }

    public LiveData<Boolean> getEnrolledExamsLoading() {
        return mLoading;
    }

    public void refreshEnrolledExams() {
        mRepository.startFetchEnrolledExamsService();
    }

}
