package it.communikein.myunimib.viewmodel.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.viewmodel.AvailableExamsListViewModel;


public class AvailableExamsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UnimibRepository mRepository;

    @Inject
    public AvailableExamsViewModelFactory(UnimibRepository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AvailableExamsListViewModel(mRepository);
    }

}
