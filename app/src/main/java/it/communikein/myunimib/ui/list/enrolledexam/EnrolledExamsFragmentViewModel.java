package it.communikein.myunimib.ui.list.enrolledexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.EnrolledExam;


class EnrolledExamsFragmentViewModel extends ViewModel {

    private final LiveData<PagedList<EnrolledExam>> mData;
    private final MutableLiveData<Integer> mChanged;

    public EnrolledExamsFragmentViewModel(UnimibRepository repository) {
        LivePagedListBuilder<Integer, EnrolledExam> builder =
                new LivePagedListBuilder<>(repository.getCurrentEnrolledExams(), /* */ 20);

        mData = builder.build();
        mChanged = repository.getModifiedEnrolledExamsCount();
    }

    public LiveData<PagedList<EnrolledExam>> getEnrolledExams() {
        return mData;
    }

    public LiveData<Integer> getModifiedEnrolledExamsCount() {
        return mChanged;
    }

    public void clearChanges() {
        mChanged.setValue(0);
    }

}
