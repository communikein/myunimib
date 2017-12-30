package it.communikein.myunimib.ui.list.booklet;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.BookletEntry;


class BookletViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final LiveData<List<BookletEntry>> mBooklet;
    private final LiveData<Boolean> mLoading;

    public BookletViewModel(UnimibRepository repository) {
        mRepository = repository;

        mBooklet = mRepository.getObservableCurrentBooklet();
        mLoading = mRepository.getBookletLoading();
    }

    public LiveData<List<BookletEntry>> getBooklet() {
        return mBooklet;
    }

    public LiveData<Boolean> getBookletLoading() {
        return mLoading;
    }

    public void refreshBooklet() {
        mRepository.startFetchBookletService();
    }

}
