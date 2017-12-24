package it.communikein.myunimib.ui.list.availableexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.AvailableExam;
import it.communikein.myunimib.data.database.UnimibDao;


class AvailableExamsFragmentViewModel extends ViewModel {

    private final LiveData<PagedList<AvailableExam>> mData;
    private final MutableLiveData<Integer> mChanged;

    public AvailableExamsFragmentViewModel(UnimibRepository repository) {
        LivePagedListBuilder<Integer, AvailableExam> builder =
                new LivePagedListBuilder<>(repository.getCurrentAvailableExams(), /* */ 20);

        mData = builder.build();
        mChanged = repository.getModifiedAvailableExamsCount();

    }

    public LiveData<PagedList<AvailableExam>> getAvailableExams() {
        return mData;
    }

    public LiveData<Integer> getModifiedAvailableExamsCount() {
        return mChanged;
    }

    public void clearChanges() {
        mChanged.setValue(0);
    }

    public void deleteExam(AvailableExam exam) {

    }

}
