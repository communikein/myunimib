package com.communikein.myunimib;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by eliam on 12/6/2017.
 */

public class BookletAdapter extends RecyclerView.Adapter<BookletAdapter.BookletAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private Cursor mCursor;

    /**
     * Creates a BookletAdapter.
     *
     * @param context Used to talk to the UI and app resources
     */
    public BookletAdapter(@NonNull Context context) {
        mContext = context;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public BookletAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.booklet_list_item, viewGroup, false);

        view.setFocusable(true);

        return new BookletAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param bookletAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(BookletAdapterViewHolder bookletAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        /* Read exam's name from the cursor */
        String name = mCursor.getString(BookletFragment.INDEX_BOOKLET_COURSE_NAME);
        String mark = mCursor.getString(BookletFragment.INDEX_BOOKLET_MARK);
        String state = mCursor.getString(BookletFragment.INDEX_BOOKLET_STATE);
        boolean passed = false;
        if (state.toLowerCase().contains("superata"))
            passed = true;

        bookletAdapterViewHolder.bookletCourseName.setText(name);
        bookletAdapterViewHolder.bookletCourseScore.setText(mark.toUpperCase());
        bookletAdapterViewHolder.bookletCourseStatus
                .setBackgroundResource(passed ? R.color.passed : R.color.waiting);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class BookletAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView bookletCourseName;
        final TextView bookletCourseScore;
        final View bookletCourseStatus;

        BookletAdapterViewHolder(View view) {
            super(view);

            bookletCourseName = view.findViewById(R.id.tv_name);
            bookletCourseScore = view.findViewById(R.id.tv_score);
            bookletCourseStatus = view.findViewById(R.id.exam_icon);
        }
    }

}
