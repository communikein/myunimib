package it.communikein.myunimib.ui.list.enrolledexam;

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
import it.communikein.myunimib.data.database.ListEnrolledExam;
import it.communikein.myunimib.utilities.MyunimibDateUtils;


public class EnrolledExamAdapter extends RecyclerView.Adapter<EnrolledExamAdapter.ExamAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private final ListItemClickListener mOnClickListener;

    private final PagedListAdapterHelper<ListEnrolledExam> mHelper;

    public interface ListItemClickListener {
        void onListItemClick(int exam_id);
    }

    private static final DiffCallback<ListEnrolledExam> DIFF_CALLBACK = new DiffCallback<ListEnrolledExam>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListEnrolledExam oldItem, @NonNull ListEnrolledExam newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListEnrolledExam oldItem, @NonNull ListEnrolledExam newItem) {
            return newItem.getAdsceId() == oldItem.getAdsceId() &&
                    newItem.getDate().equals(oldItem.getDate()) &&
                    newItem.getDescription().equals(oldItem.getDescription()) &&
                    newItem.getName().equals(oldItem.getName());
        }
    };

    /**
     * Creates a BookletAdapter.
     *
     * @param context Used to talk to the UI and app resources
     */
    EnrolledExamAdapter(@NonNull Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;

        mHelper = new PagedListAdapterHelper<>(this, DIFF_CALLBACK);
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
     * @param holder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ExamAdapterViewHolder holder, int position) {
        ListEnrolledExam exam = mHelper.getItem(position);
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

    public void setList(PagedList<ListEnrolledExam> pagedList) {
        mHelper.setList(pagedList);
    }


    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class ExamAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
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
            if (mOnClickListener != null)
                mOnClickListener.onListItemClick(exam_adsce_id);
        }

        void bindTo(ListEnrolledExam entry) {
            String friendly_date = MyunimibDateUtils
                    .getFriendlyDateString(mContext, entry.getDate().getTime(), false);

            exam_adsce_id = entry.getAdsceId();
            corseNameTextView.setText(entry.getName());
            descriptionTextView.setText(entry.getDescription());
            dateTextView.setText(friendly_date);
        }

        void clear() {

        }
    }

}
