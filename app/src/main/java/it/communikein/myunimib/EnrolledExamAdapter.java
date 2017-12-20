package it.communikein.myunimib;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.communikein.myunimib.utilities.MyunimibDateUtils;


public class EnrolledExamAdapter extends RecyclerView.Adapter<EnrolledExamAdapter.ExamAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private final ListItemClickListener mOnClickListener;

    private Cursor mCursor;

    public interface ListItemClickListener {
        void onListItemClick(int exam_id);
    }

    /**
     * Creates a BookletAdapter.
     *
     * @param context Used to talk to the UI and app resources
     */
    EnrolledExamAdapter(@NonNull Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    @Override
    public ExamAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.enrolled_exam_list_item, parent, false);

        view.setFocusable(true);

        return new ExamAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param examAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ExamAdapterViewHolder examAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        /* Read exam's name from the cursor */
        int exam_adsce_id = mCursor.getInt(EnrolledExamsFragment.INDEX_EXAM_ADSCE_ID);
        String name = mCursor.getString(EnrolledExamsFragment.INDEX_EXAM_COURSE_NAME);
        String description = mCursor.getString(EnrolledExamsFragment.INDEX_EXAM_DESCRIPTION);
        long date = (long) mCursor.getInt(EnrolledExamsFragment.INDEX_EXAM_DATE);

        examAdapterViewHolder.exam_adsce_id = exam_adsce_id;
        examAdapterViewHolder.corseNameTextView.setText(name);
        examAdapterViewHolder.descriptionTextView.setText(description);
        examAdapterViewHolder.dateTextView.setText(MyunimibDateUtils
                .getFriendlyDateString(mContext, date, false));
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
    class ExamAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int exam_adsce_id;

        final TextView corseNameTextView;
        final TextView descriptionTextView;
        final TextView dateTextView;

        ExamAdapterViewHolder(View view) {
            super(view);

            corseNameTextView = view.findViewById(R.id.tv_name);
            descriptionTextView = view.findViewById(R.id.tv_description);
            dateTextView = view.findViewById(R.id.tv_date);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onListItemClick(exam_adsce_id);
        }
    }

}
