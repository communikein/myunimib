package it.communikein.myunimib.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.loaders.EnrollLoader.EnrollUpdatesListener;
import it.communikein.myunimib.data.network.loaders.EnrollLoader;


public class AvailableExamDetailViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private LiveData<AvailableExam> mExam;

    @Inject
    public AvailableExamDetailViewModel(UnimibRepository repository) {
        this.mRepository = repository;
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

    public User getUser() {
        return mRepository.getUser(null);
    }


    public EnrollLoader enroll(Activity activity, EnrollUpdatesListener callback) {
        AvailableExam exam = mExam.getValue();

        return mRepository.enrollExam(exam, activity, callback);
    }

    public void refreshAvailableExams() {
        mRepository.fetchAvailableExams();
    }

    public void refreshEnrolledExams() {
        mRepository.fetchEnrolledExams();
    }
}
