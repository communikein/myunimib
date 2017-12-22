package it.communikein.myunimib.ui.list.enrolledexam;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.ListEnrolledExam;


class EnrolledExamsFragmentViewModel extends ViewModel {

    private final LiveData<PagedList<ListEnrolledExam>> mData;

    public EnrolledExamsFragmentViewModel(UnimibRepository repository) {
        LivePagedListBuilder<Integer, ListEnrolledExam> builder =
                new LivePagedListBuilder<>(repository.getCurrentEnrolledExams(), /* */ 20);
        mData = builder.build();
    }

    public LiveData<PagedList<ListEnrolledExam>> getEnrolledExams() {
        return mData;
    }

}
