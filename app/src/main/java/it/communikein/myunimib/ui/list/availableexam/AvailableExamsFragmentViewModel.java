package it.communikein.myunimib.ui.list.availableexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ListAvailableExam;


class AvailableExamsFragmentViewModel extends ViewModel {

    private final LiveData<PagedList<ListAvailableExam>> mData;

    public AvailableExamsFragmentViewModel(UnimibRepository repository) {
        LivePagedListBuilder<Integer, ListAvailableExam> builder =
                new LivePagedListBuilder<>(repository.getCurrentAvailableExams(), /* */ 20);
        mData = builder.build();
    }

    public LiveData<PagedList<ListAvailableExam>> getAvailableExams() {
        return mData;
    }

}
