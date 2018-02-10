package it.communikein.myunimib.ui.list.booklet;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.databinding.FragmentExamsBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.viewmodel.BookletViewModel;
import it.communikein.myunimib.viewmodel.factory.BookletViewModelFactory;


/**
 * The {@link Fragment} responsible for showing the user's booklet.
 */
public class BookletFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener, BookletAdapter.ExamClickCallback {

    private static final String LOG_TAG = BookletFragment.class.getSimpleName();

    /*  */
    private FragmentExamsBinding mBinding;

    /* */
    @Inject
    BookletViewModelFactory viewModelFactory;

    /* */
    private BookletViewModel mViewModel;

    /* Required empty public constructor */
    public BookletFragment() {}


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_exams, container, false);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list.
         *
         * The third parameter (reverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        mBinding.rvList.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.rvList.setHasFixedSize(true);

        /* Show data downloading */
        mBinding.swipeRefresh.setOnRefreshListener(this);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(BookletViewModel.class);

        /*
         * Ensures a loader is initialized and active and shows the loading view.
         * If the loader doesn't already exist, one is created and (if the activity/fragment is
         * currently started) starts the loader. Otherwise the last created loader is re-used.
         */
        if (getActivity() != null && !mViewModel.getUser().isFake()) {

            /* Create a new BookletAdapter. It will be responsible for displaying the list's items */
            final BookletAdapter mAdapter = new BookletAdapter(this);

            mViewModel.getBookletLoading().observe(this, loading -> {
                if (loading != null)
                    mBinding.swipeRefresh.setRefreshing(loading);
            });

            mViewModel.getBooklet().observe(this, list -> {
                if (list != null) {
                    Log.d(LOG_TAG, "Updating the booklet list. " + list.size() + " elements.");
                    mAdapter.setList((ArrayList<BookletEntry>) list);
                }
            });

            /* Setting the adapter attaches it to the RecyclerView in our layout. */
            mBinding.rvList.setAdapter(mAdapter);
        }
    }


    @Override
    public void onRefresh() {
        mViewModel.refreshBooklet();
    }

    /**
     * Change the Activity's ActionBar title.
     */
    private void setTitle() {
        if (getActivity() != null) {
            /* Get a reference to the MainActivity ActionBar */
            ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
            /* If there is an ActionBar, set it's title */
            if (actionBar != null)
                actionBar.setTitle(R.string.title_booklet);
        }
    }

    @Override
    public void onListItemClick(int adsce_id) {}
}
