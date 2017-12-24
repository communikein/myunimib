package it.communikein.myunimib.ui.list.booklet;

import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapterHelper;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.DiffCallback;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.BookletEntry;
import it.communikein.myunimib.ui.list.booklet.BookletAdapter.BookletAdapterViewHolder;


class BookletAdapter extends RecyclerView.Adapter<BookletAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private final ListItemClickListener mOnClickListener;

    private final PagedListAdapterHelper<BookletEntry> mHelper;

    public interface ListItemClickListener {
        void onListItemClick(int adsce_id);
    }

    private static final DiffCallback<BookletEntry> DIFF_CALLBACK = new DiffCallback<BookletEntry>() {
        @Override
        public boolean areItemsTheSame(@NonNull BookletEntry oldItem, @NonNull BookletEntry newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull BookletEntry oldItem, @NonNull BookletEntry newItem) {
            return newItem.getName().equals(oldItem.getName()) &&
                    newItem.getScore().equals(oldItem.getScore()) &&
                    newItem.getState().equals(oldItem.getState());
        }
    };

    /**
     * Creates a BookletAdapter.
     *
     * @param context Used to talk to the UI and app resources
     */
    BookletAdapter(@NonNull Context context, ListItemClickListener handler) {
        mHelper = new PagedListAdapterHelper<>(this, DIFF_CALLBACK);

        mContext = context;
        mOnClickListener = handler;
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
        View view = LayoutInflater.from(mContext)
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
     * @param holder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(BookletAdapterViewHolder holder, int position) {
        BookletEntry exam = mHelper.getItem(position);
        if (exam != null)
            holder.bindTo(exam);
        else
            holder.clear();
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return mHelper.getItemCount();
    }

    public void setList(PagedList<BookletEntry> pagedList) {
        mHelper.setList(pagedList);
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class BookletAdapterViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
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

        @Override
        public void onClick(View view) {
            if (mOnClickListener != null)
                mOnClickListener.onListItemClick(exam_adsce_id);
        }

        void bindTo(BookletEntry entry) {
            boolean passed = false;
            if (entry.getState().toLowerCase().contains("superata"))
                passed = true;

            exam_adsce_id = entry.getAdsceId();
            bookletCourseName.setText(entry.getName());
            bookletCourseScore.setText(entry.getScore().toUpperCase());
            bookletCourseStatus.setBackgroundResource(passed ? R.color.passed : R.color.waiting);
        }

        void clear() {

        }
    }

}
