package it.communikein.myunimib.ui.enrolledexam;

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
import it.communikein.myunimib.data.database.ListEnrolledExam;
import it.communikein.myunimib.utilities.MyunimibDateUtils;


public class EnrolledExamAdapter extends RecyclerView.Adapter<EnrolledExamAdapter.ExamAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private final ListItemClickListener mOnClickListener;

    private ArrayList<ListEnrolledExam> mData;

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
        ListEnrolledExam exam = mData.get(position);

        examAdapterViewHolder.exam_adsce_id = exam.getAdsceId();
        examAdapterViewHolder.corseNameTextView.setText(exam.getName());
        examAdapterViewHolder.descriptionTextView.setText(exam.getDescription());
        examAdapterViewHolder.dateTextView.setText(MyunimibDateUtils
                .getFriendlyDateString(mContext, exam.getDate().getTime(), false));
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
    void swapData(final List<ListEnrolledExam> exams) {
        if (mData == null) {
            mData = (ArrayList<ListEnrolledExam>) exams;
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
                    ListEnrolledExam newEntry = exams.get(newItemPosition);
                    ListEnrolledExam oldEntry = exams.get(oldItemPosition);

                    return newEntry.getAdsceId() == oldEntry.getAdsceId() &&
                            newEntry.getDate().equals(oldEntry.getDate()) &&
                            newEntry.getDescription().equals(oldEntry.getDescription()) &&
                            newEntry.getName().equals(oldEntry.getName());
                }
            });

            mData = (ArrayList<ListEnrolledExam>) exams;
            result.dispatchUpdatesTo(this);
        }
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
