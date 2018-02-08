package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.Building;

public class BuildingsViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final List<Building> mData;

    @Inject
    public BuildingsViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = repository.getCurrentBuildings();
    }

    public List<Building> getBuildings() {
        return mData;
    }

    public UnimibRepository getRepository() {
        return mRepository;
    }

}
