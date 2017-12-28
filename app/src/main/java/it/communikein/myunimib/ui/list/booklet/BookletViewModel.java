package it.communikein.myunimib.ui.list.booklet;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.BookletEntry;


class BookletViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final LiveData<List<BookletEntry>> mBooklet;
    private final MutableLiveData<Integer> mChanges;
    private final LiveData<Boolean> mLoading;

    public BookletViewModel(UnimibRepository repository) {
        mRepository = repository;

        mBooklet = mRepository.getCurrentBooklet();
        mChanges = mRepository.getModifiedBookletEntriesCount();
        mLoading = mRepository.getBookletLoading();
    }

    public LiveData<List<BookletEntry>> getBooklet() {
        return mBooklet;
    }

    public LiveData<Integer> getModifiedBookletEntriesCount() {
        return mChanges;
    }

    public LiveData<Boolean> getBookletLoading() {
        return mLoading;
    }

    public void clearChanges() {
        mChanges.setValue(0);
    }

    public void refreshBooklet() {
        mRepository.startFetchBookletService();
    }

}
