package it.communikein.myunimib.ui.list.booklet;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.BookletEntry;


class BookletFragmentViewModel extends ViewModel {

    private final LiveData<PagedList<BookletEntry>> mData;
    private final MutableLiveData<Integer> mChanged;

    public BookletFragmentViewModel(UnimibRepository repository) {
        LivePagedListBuilder<Integer, BookletEntry> builder =
                new LivePagedListBuilder<>(repository.getCurrentBooklet(), /* */ 20);

        mData = builder.build();
        mChanged = repository.getModifiedBookletEntriesCount();
    }

    public LiveData<PagedList<BookletEntry>> getBooklet() {
        return mData;
    }

    public LiveData<Integer> getModifiedBookletEntriesCount() {
        return mChanged;
    }

    public void clearChanges() {
        mChanged.setValue(0);
    }

}
