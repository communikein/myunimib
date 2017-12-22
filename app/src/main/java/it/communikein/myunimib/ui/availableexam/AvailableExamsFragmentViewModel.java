package it.communikein.myunimib.ui.availableexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ListAvailableExam;


class AvailableExamsFragmentViewModel extends ViewModel {

    private final LiveData<List<ListAvailableExam>> mData;

    public AvailableExamsFragmentViewModel(UnimibRepository repository) {
        mData = repository.getCurrentAvailableExams();
    }

    public LiveData<List<ListAvailableExam>> getAvailableExams() {
        return mData;
    }

}
