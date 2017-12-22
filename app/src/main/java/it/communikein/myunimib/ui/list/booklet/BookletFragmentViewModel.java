package it.communikein.myunimib.ui.list.booklet;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ListBookletEntry;


class BookletFragmentViewModel extends ViewModel {

    private final LiveData<PagedList<ListBookletEntry>> mData;

    public BookletFragmentViewModel(UnimibRepository repository) {
        LivePagedListBuilder<Integer, ListBookletEntry> builder =
                new LivePagedListBuilder<>(repository.getCurrentBooklet(), /* */ 20);
        mData = builder.build();
    }

    public LiveData<PagedList<ListBookletEntry>> getBooklet() {
        return mData;
    }

}
