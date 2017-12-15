package com.communikein.myunimib;


import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.communikein.myunimib.data.ExamContract;
import com.communikein.myunimib.databinding.FragmentBookletBinding;
import com.communikein.myunimib.sync.booklet.SyncUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookletFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * weather data.
     */
    public static final String[] BOOKLET_PROJECTION = {
            ExamContract.BookletEntry.COLUMN_COURSE_NAME,
            ExamContract.BookletEntry.COLUMN_MARK,
            ExamContract.BookletEntry.COLUMN_STATE
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_BOOKLET_COURSE_NAME = 0;
    public static final int INDEX_BOOKLET_MARK = 1;
    public static final int INDEX_BOOKLET_STATE = 2;


    static final int ID_BOOKLET_LOADER = 44;

    private BookletAdapter mBookletAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    FragmentBookletBinding mBinding;

    public BookletFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_booklet, container, false);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mBinding.rvList.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.rvList.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * MainActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        mBookletAdapter = new BookletAdapter(getActivity());

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mBinding.rvList.setAdapter(mBookletAdapter);


        toggleLoading(true);

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getActivity().getSupportLoaderManager().initLoader(ID_BOOKLET_LOADER, null, this);

        SyncUtils.initialize(getActivity());
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_BOOKLET_LOADER:
                /* URI for all rows of weather data in our weather table */
                Uri bookletQueryUri = ExamContract.BookletEntry.CONTENT_URI;

                return new CursorLoader(getActivity(),
                        bookletQueryUri,
                        BOOKLET_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBookletAdapter.swapCursor(data);

        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;

        mBinding.rvList.smoothScrollToPosition(mPosition);

        toggleLoading(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mBookletAdapter.swapCursor(null);
    }

    private void toggleLoading(boolean show) {
        /* Then, hide the weather data */
        mBinding.rvList.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        /* Finally, show the loading indicator */
        mBinding.pbLoadingIndicator.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
