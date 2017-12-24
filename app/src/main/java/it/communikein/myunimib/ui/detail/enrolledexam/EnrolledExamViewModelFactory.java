package it.communikein.myunimib.ui.detail.enrolledexam;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ExamID;

/**
 * Created by eliam on 12/22/2017.
 */

public class EnrolledExamViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UnimibRepository mRepository;
    private final ExamID mExamId;

    public EnrolledExamViewModelFactory(UnimibRepository repository, ExamID examID) {
        this.mRepository = repository;

        this.mExamId = examID;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EnrolledExamDetailActivityViewModel(mRepository, mExamId);
    }

}
