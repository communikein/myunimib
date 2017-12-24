package it.communikein.myunimib.ui.list.booklet;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.communikein.myunimib.R;
import it.communikein.myunimib.databinding.FragmentBookletBinding;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.utilities.InjectorUtils;
import it.communikein.myunimib.utilities.NotificationHelper;
import it.communikein.myunimib.utilities.UserUtils;


/**
 * The {@link Fragment} responsible for showing the user's booklet.
 */
public class BookletFragment extends Fragment {

    /*  */
    private BookletAdapter mAdapter;

    /*  */
    private FragmentBookletBinding mBinding;

    /* */
    private BookletFragmentViewModel mViewModel;

    /* Required empty public constructor */
    public BookletFragment() {}


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

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.rvList.setHasFixedSize(true);

        /* Create a new BookletAdapter. It will be responsible for displaying the list's items */
        if (getActivity() != null)
            mAdapter = new BookletAdapter(getActivity(), null);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();

        /*
         * Ensures a loader is initialized and active and shows the loading view.
         * If the loader doesn't already exist, one is created and (if the activity/fragment is
         * currently started) starts the loader. Otherwise the last created loader is re-used.
         */
        if (!UserUtils.getUser(getActivity()).isFake()) {
            BookletViewModelFactory factory = InjectorUtils
                    .provideBookletViewModelFactory(this.getContext());
            mViewModel = ViewModelProviders.of(this, factory)
                    .get(BookletFragmentViewModel.class);

            mViewModel.getBooklet().observe(this,
                    pagedList -> mAdapter.setList(pagedList));

            mViewModel.getModifiedBookletEntriesCount().observe(this, count -> {
                if (getActivity() != null && count != null && count > 0) {
                    createEntriesModifiedNotification(getActivity(), count);
                    mViewModel.clearChanges();
                }
            });

            /* Setting the adapter attaches it to the RecyclerView in our layout. */
            mBinding.rvList.setAdapter(mAdapter);
        }
    }

    private void createEntriesModifiedNotification(@NonNull Context context, int count) {
        String title = context.getString(R.string.channel_booklet_name);
        String content = context.getString(R.string.channel_booklet_content_changes);
        int notificationId = 1;

        NotificationHelper notificationHelper = new NotificationHelper(getActivity());
        notificationHelper.notify(notificationId,
                notificationHelper.getNotificationBooklet(title, content));
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
}
