package it.communikein.myunimib.ui.list.availableexam;

import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapterHelper;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.DiffCallback;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.AvailableExam;
import it.communikein.myunimib.databinding.AvailableExamListItemBinding;
import it.communikein.myunimib.utilities.MyunimibDateUtils;


public class AvailableExamAdapter extends RecyclerView.Adapter<AvailableExamAdapter.ExamAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private final ListItemClickListener mOnClickListener;

    private final PagedListAdapterHelper<AvailableExam> mHelper;

    public interface ListItemClickListener {
        void onListItemClick(AvailableExam exam);
        void onEnrollmentClicked(AvailableExam exam);
    }

    private static final DiffCallback<AvailableExam> DIFF_CALLBACK = new DiffCallback<AvailableExam>() {
        @Override
        public boolean areItemsTheSame(@NonNull AvailableExam oldItem, @NonNull AvailableExam newItem) {
            return newItem.equals(oldItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull AvailableExam oldItem, @NonNull AvailableExam newItem) {
            return newItem.getBeginEnrollment().getTime() == oldItem.getBeginEnrollment().getTime() &&
                    newItem.getEndEnrollment().getTime() == oldItem.getEndEnrollment().getTime() &&
                    newItem.getName().equals(oldItem.getName());
        }
    };

    /**
     * Creates a BookletAdapter.
     *
     * @param context Used to talk to the UI and app resources
     */
    AvailableExamAdapter(@NonNull Context context, ListItemClickListener listItemClickListener) {
        mContext = context;

        mOnClickListener = listItemClickListener;

        mHelper = new PagedListAdapterHelper<>(this, DIFF_CALLBACK);
    }

    @Override
    public ExamAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.available_exam_list_item, parent, false);
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
        AvailableExam exam = mHelper.getItem(position);
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

    public void setList(PagedList<AvailableExam> pagedList) {
        mHelper.setList(pagedList);
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class ExamAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private AvailableExam mExam;

        private final TextView nameTextview;
        private final TextView examDescriptionTextview;
        private final TextView beginEnrollmentTextview;
        private final TextView endEnrollmentTextview;
        private final Button enrollButton;

        ExamAdapterViewHolder(View view) {
            super(view);

            nameTextview = view.findViewById(R.id.name_textview);
            examDescriptionTextview = view.findViewById(R.id.exam_description_textview);
            beginEnrollmentTextview = view.findViewById(R.id.begin_enrollment_textview);
            endEnrollmentTextview = view.findViewById(R.id.end_enrollment_textview);
            enrollButton = view.findViewById(R.id.enroll_button);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnClickListener != null && mExam!= null)
                mOnClickListener.onListItemClick(mExam);
        }

        void bindTo(final AvailableExam entry) {
            String friendly_date_begin = MyunimibDateUtils.getFriendlyDateString(
                    mContext,
                    entry.getBeginEnrollment().getTime(),
                    false,
                    false);
            String friendly_date_end = MyunimibDateUtils.getFriendlyDateString(
                    mContext,
                    entry.getEndEnrollment().getTime(),
                    false,
                    false);

            this.mExam = entry;
            nameTextview.setText(entry.getName());
            examDescriptionTextview.setText(entry.getDescription());
            beginEnrollmentTextview.setText(friendly_date_begin);
            endEnrollmentTextview.setText(friendly_date_end);

            enrollButton.setOnClickListener(v -> {
                if (mOnClickListener != null)
                    mOnClickListener.onEnrollmentClicked(entry);
            });
        }

        void clear() {

        }
    }

}
