package it.communikein.myunimib.ui.booklet;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ListBookletEntry;


class BookletFragmentViewModel extends ViewModel {

    private final LiveData<List<ListBookletEntry>> mData;

    public BookletFragmentViewModel(UnimibRepository repository) {
        mData = repository.getCurrentBooklet();
    }

    public LiveData<List<ListBookletEntry>> getBooklet() {
        return mData;
    }

}
