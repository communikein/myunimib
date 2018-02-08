package it.communikein.myunimib.viewmodel;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.accountmanager.AccountUtils;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.User;


public class EnrolledExamsListViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final LiveData<List<EnrolledExam>> mData;
    private final LiveData<Boolean> mLoading;

    @Inject
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

    public User getUser() {
        return mRepository.getUser();
    }

}
