package it.communikein.myunimib.ui.detail.enrolledexam;

import android.arch.lifecycle.ViewModel;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.EnrolledExam;
import it.communikein.myunimib.data.database.ExamID;


public class EnrolledExamDetailViewModel extends ViewModel {

    private final EnrolledExam mData;

    public EnrolledExamDetailViewModel(UnimibRepository repository, ExamID examID) {
        mData = repository.getEnrolledExam(examID);
    }

    public EnrolledExam getExam() {
        return mData;
    }

    public ExamID getExamId() {
        return mData;
    }

}
