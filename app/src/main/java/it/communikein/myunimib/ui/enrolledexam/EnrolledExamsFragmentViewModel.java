package it.communikein.myunimib.ui.enrolledexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ListEnrolledExam;


class EnrolledExamsFragmentViewModel extends ViewModel {

    private final LiveData<List<ListEnrolledExam>> mData;

    public EnrolledExamsFragmentViewModel(UnimibRepository repository) {
        mData = repository.getCurrentEnrolledExams();
    }

    public LiveData<List<ListEnrolledExam>> getEnrolledExams() {
        return mData;
    }

}
