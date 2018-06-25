package it.communikein.myunimib.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.loaders.CertificateLoader;
import it.communikein.myunimib.data.network.loaders.UnEnrollLoader;
import it.communikein.myunimib.data.network.loaders.UnEnrollLoader.UnEnrollUpdatesListener;


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

    public Building getBuilding(String name) {
        return mRepository.getBuilding(name);
    }

    public UnEnrollLoader unEnroll(Activity activity, UnEnrollUpdatesListener callback){
        return mRepository.unEnrollExam(mExam.getValue(), activity, callback);
    }


    public CertificateLoader loadCertificate(Activity activity) {
        return mRepository.loadCertificate(mExam.getValue(), activity);
    }


    public User getUser() {
        return mRepository.getUser(null);
    }

}
