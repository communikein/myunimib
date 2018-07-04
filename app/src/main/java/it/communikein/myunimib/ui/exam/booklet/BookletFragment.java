package it.communikein.myunimib.ui.exam.booklet;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.databinding.FragmentBookletBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.viewmodel.MainActivityViewModel;


/**
 * The {@link Fragment} responsible for showing the user's booklet.
 */
public class BookletFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener, BookletAdapter.ExamClickCallback {

    public static final String TAG = BookletFragment.class.getSimpleName();

    /*  */
    private FragmentBookletBinding mBinding;

    private BookletAdapter mAdapter;

    /* Required empty public constructor */
    public BookletFragment() {}

    public MainActivityViewModel getViewModel() {
        return ((MainActivity) getActivity()).getViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_booklet, container, false);

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

        /* Create a new BookletAdapter. It will be responsible for displaying the list's items */
        mAdapter = new BookletAdapter(this);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.rvList.setHasFixedSize(true);

        /* Show data downloading */
        mBinding.swipeRefresh.setOnRefreshListener(this);

        /* Hide FAB until we know if the user is guest or not */
        mBinding.fab.setVisibility(View.GONE);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();

        getViewModel().getUser().observe(this, (user) -> {
            if (user != null) {
                if (!user.isFake()) mBinding.fab.setVisibility(View.VISIBLE);

                getViewModel().getBookletLoading().observe(this, loading -> {
                    if (loading != null)
                        mBinding.swipeRefresh.setRefreshing(loading);
                });

                getViewModel().getBooklet().observe(this, list -> {
                    if (list != null) {
                        mAdapter.setList((ArrayList<BookletEntry>) list);
                    }
                });

                /* Setting the adapter attaches it to the RecyclerView in our layout. */
                mBinding.rvList.setAdapter(mAdapter);
            }
        });
    }


    @Override
    public void onRefresh() {
        getViewModel().refreshBooklet();
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
