package it.communikein.myunimib.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.User;


public class BookletViewModel extends ViewModel {

    private final UnimibRepository mRepository;

    private final LiveData<List<BookletEntry>> mBooklet;
    private final LiveData<Boolean> mLoading;

    @Inject
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
        mRepository.fetchBooklet();
    }

    public User getUser() {
        return mRepository.getUser(null);
    }

}
