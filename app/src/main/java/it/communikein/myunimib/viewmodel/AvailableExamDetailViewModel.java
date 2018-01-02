package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.AvailableExam;
import it.communikein.myunimib.data.database.ExamID;


public class AvailableExamDetailViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private LiveData<AvailableExam> mExam;

    @Inject
    public AvailableExamDetailViewModel(UnimibRepository repository) {
        this.mRepository = repository;
        this.mExam = null;
    }

    public void setExamId(ExamID examId) {
        this.mExam = mRepository.getObservableAvailableExam(examId);
    }

    public LiveData<AvailableExam> getExam() {
        return mExam;
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
