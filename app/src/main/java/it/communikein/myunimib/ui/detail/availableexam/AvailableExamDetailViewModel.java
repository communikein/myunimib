package it.communikein.myunimib.ui.detail.availableexam;

import android.arch.lifecycle.ViewModel;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.AvailableExam;
import it.communikein.myunimib.data.database.ExamID;


public class AvailableExamDetailViewModel extends ViewModel {

    private final AvailableExam mData;

    public AvailableExamDetailViewModel(UnimibRepository repository, ExamID examID) {
        mData = repository.getAvailableExam(examID);
    }

    public AvailableExam getExam() {
        return mData;
    }

    public ExamID getExamId() {
        return mData;
    }

}
