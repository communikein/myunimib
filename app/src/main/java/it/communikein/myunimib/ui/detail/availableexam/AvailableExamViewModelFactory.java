package it.communikein.myunimib.ui.detail.availableexam;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.Exam;
import it.communikein.myunimib.data.database.ExamID;
import it.communikein.myunimib.ui.detail.enrolledexam.EnrolledExamDetailActivityViewModel;

/**
 * Created by eliam on 12/23/2017.
 */

public class AvailableExamViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UnimibRepository mRepository;

    private final ExamID examID;

    public AvailableExamViewModelFactory(UnimibRepository repository, ExamID examID) {
        this.mRepository = repository;

        this.examID = examID;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AvailableExamDetailViewModel(mRepository, examID);
    }

}
