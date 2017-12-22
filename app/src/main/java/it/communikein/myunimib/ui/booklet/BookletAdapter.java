package it.communikein.myunimib.ui.booklet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.ListBookletEntry;


public class BookletAdapter extends RecyclerView.Adapter<BookletAdapter.BookletAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private ArrayList<ListBookletEntry> mData;

    /**
     * Creates a BookletAdapter.
     *
     * @param context Used to talk to the UI and app resources
     */
    BookletAdapter(@NonNull Context context) {
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
        ListBookletEntry exam = mData.get(position);
        boolean passed = false;
        if (exam.getState().toLowerCase().contains("superata"))
            passed = true;

        bookletAdapterViewHolder.exam_adsce_id = exam.getAdsceId();
        bookletAdapterViewHolder.bookletCourseName.setText(exam.getName());
        bookletAdapterViewHolder.bookletCourseScore.setText(exam.getScore().toUpperCase());
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
        if (null == mData) return 0;
        return mData.size();
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param exams the new exams to use as the adapter's data source
     */
    void swapData(final List<ListBookletEntry> exams) {
        if (mData == null) {
            mData = (ArrayList<ListBookletEntry>) exams;
            notifyDataSetChanged();
        }
        else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mData.size();
                }

                @Override
                public int getNewListSize() {
                    return mData.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mData.get(oldItemPosition).equals(mData.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    ListBookletEntry newEntry = exams.get(newItemPosition);
                    ListBookletEntry oldEntry = exams.get(oldItemPosition);

                    return newEntry.getName().equals(oldEntry.getName()) &&
                            newEntry.getScore().equals(oldEntry.getScore()) &&
                            newEntry.getState().equals(oldEntry.getState());
                }
            });

            mData = (ArrayList<ListBookletEntry>) exams;
            result.dispatchUpdatesTo(this);
        }
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class BookletAdapterViewHolder extends RecyclerView.ViewHolder {
        int exam_adsce_id;

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
