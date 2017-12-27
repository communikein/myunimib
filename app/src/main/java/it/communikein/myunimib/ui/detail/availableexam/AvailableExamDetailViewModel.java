package it.communikein.myunimib.ui.detail.availableexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.AvailableExam;
import it.communikein.myunimib.data.database.ExamID;


public class AvailableExamDetailViewModel extends ViewModel {

    private final LiveData<AvailableExam> exam;

    public AvailableExamDetailViewModel(UnimibRepository repository, ExamID examID) {
        exam = repository.getAvailableExam(examID);
    }

    public LiveData<AvailableExam> getExam() {
        return exam;
    }

}
