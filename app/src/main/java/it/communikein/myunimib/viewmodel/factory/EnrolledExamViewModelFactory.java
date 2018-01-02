package it.communikein.myunimib.viewmodel.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ExamID;
import it.communikein.myunimib.viewmodel.EnrolledExamDetailViewModel;

/**
 * Created by eliam on 12/22/2017.
 */

public class EnrolledExamViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UnimibRepository mRepository;

    @Inject
    public EnrolledExamViewModelFactory(UnimibRepository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EnrolledExamDetailViewModel(mRepository);
    }

}
