package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.Building;

public class BuildingsViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final List<Building> mData;
    private MutableLiveData<Building> selectedBuilding;

    @Inject
    public BuildingsViewModel(UnimibRepository repository) {
        mRepository = repository;

        mData = repository.getCurrentBuildings();
        selectedBuilding = new MutableLiveData<>();
    }

    public List<Building> getBuildings() {
        return mData;
    }

    public LiveData<Building> getSelectedBuilding() {
        return selectedBuilding;
    }

    public void setSelectedBuilding(Building building) {
        this.selectedBuilding.postValue(building);
    }

    public UnimibRepository getRepository() {
        return mRepository;
    }

}
