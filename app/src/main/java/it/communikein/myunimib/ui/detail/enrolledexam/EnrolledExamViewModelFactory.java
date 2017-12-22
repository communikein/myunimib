package it.communikein.myunimib.ui.detail.enrolledexam;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import it.communikein.myunimib.data.UnimibRepository;

/**
 * Created by eliam on 12/22/2017.
 */

public class EnrolledExamViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UnimibRepository mRepository;
    private final int adsceId;

    public EnrolledExamViewModelFactory(UnimibRepository repository, int adsceId) {
        this.mRepository = repository;
        this.adsceId = adsceId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EnrolledExamDetailActivityViewModel(mRepository, adsceId);
    }

}
