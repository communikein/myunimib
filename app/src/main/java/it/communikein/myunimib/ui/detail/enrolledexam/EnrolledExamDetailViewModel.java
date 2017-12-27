package it.communikein.myunimib.ui.detail.enrolledexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.EnrolledExam;
import it.communikein.myunimib.data.database.ExamID;


public class EnrolledExamDetailViewModel extends ViewModel {

    private final LiveData<EnrolledExam> exam;

    public EnrolledExamDetailViewModel(UnimibRepository repository, ExamID examID) {
        exam = repository.getEnrolledExam(examID);
    }

    public LiveData<EnrolledExam> getExam() {
        return exam;
    }

}
