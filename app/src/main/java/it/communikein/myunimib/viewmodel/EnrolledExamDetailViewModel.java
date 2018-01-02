package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.EnrolledExam;
import it.communikein.myunimib.data.database.ExamID;


public class EnrolledExamDetailViewModel extends ViewModel {

    private final UnimibRepository mRepository;
    private LiveData<EnrolledExam> mExam;

    @Inject
    public EnrolledExamDetailViewModel(UnimibRepository repository) {
        mRepository = repository;

        mExam = null;
    }

    public void setExamId(ExamID examId) {
        mExam = mRepository.getObservableEnrolledExam(examId);
    }

    public LiveData<EnrolledExam> getExam() {
        return mExam;
    }

}
