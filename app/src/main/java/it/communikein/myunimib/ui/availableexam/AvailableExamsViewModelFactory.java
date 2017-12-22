package it.communikein.myunimib.ui.availableexam;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import it.communikein.myunimib.data.UnimibRepository;


public class AvailableExamsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UnimibRepository mRepository;

    public AvailableExamsViewModelFactory(UnimibRepository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AvailableExamsFragmentViewModel(mRepository);
    }

}
