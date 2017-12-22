package it.communikein.myunimib.ui.detail.enrolledexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.EnrolledExam;


public class EnrolledExamDetailActivityViewModel extends ViewModel {

    private final LiveData<EnrolledExam> mData;

    public EnrolledExamDetailActivityViewModel(UnimibRepository repository, int adsceId) {
        mData = repository.getEnrolledExam(adsceId);
    }

    public LiveData<EnrolledExam> getExam() {
        return mData;
    }

}
