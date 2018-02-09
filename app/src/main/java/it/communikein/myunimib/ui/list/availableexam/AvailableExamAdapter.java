package it.communikein.myunimib.ui.list.availableexam;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.Exam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.databinding.ListItemAvailableExamBinding;
import it.communikein.myunimib.utilities.DateHelper;

import it.communikein.myunimib.ui.list.availableexam.AvailableExamAdapter.ExamAdapterViewHolder;


public class AvailableExamAdapter extends RecyclerView.Adapter<ExamAdapterViewHolder> {

    @Nullable
    private final ExamClickCallback mExamClickCallback;

    private ArrayList<AvailableExam> mList;

    public interface ExamClickCallback {
        void onListItemClick(ExamID examId);
        void onEnrollmentClicked(Exam exam);
    }


    /**
     * Creates a BookletAdapter.
     *
     * @param examClickCallback Used to talk to the UI and app resources
     */
    AvailableExamAdapter(@Nullable ExamClickCallback examClickCallback) {
        mExamClickCallback = examClickCallback;
    }

    @Override
    public ExamAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemAvailableExamBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.list_item_available_exam,
                        parent, false);
        binding.setCallback(mExamClickCallback);

        return new ExamAdapterViewHolder(binding);
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
        holder.mBinding.setExam(mList.get(position));
        holder.bindToData();
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setList(ArrayList<AvailableExam> newList) {
        if (mList == null) {
            mList = newList;
            notifyItemRangeInserted(0, mList.size());
        }
        else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return newList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).equals(newList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    AvailableExam newItem = newList.get(newItemPosition);
                    AvailableExam oldItem = mList.get(oldItemPosition);
                    return newItem.getBeginEnrollment().getTime() == oldItem.getBeginEnrollment().getTime() &&
                            newItem.getEndEnrollment().getTime() == oldItem.getEndEnrollment().getTime() &&
                            newItem.getName().equals(oldItem.getName());
                }
            });
            mList = newList;
            result.dispatchUpdatesTo(this);
        }
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class ExamAdapterViewHolder extends RecyclerView.ViewHolder {

        final ListItemAvailableExamBinding mBinding;

        ExamAdapterViewHolder(ListItemAvailableExamBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
        }

        void bindToData() {
            AvailableExam entry = mBinding.getExam();
            Context context = mBinding.dataContainer.getContext();

            String friendly_date_begin = DateHelper.getFriendlyDateString(
                    context,
                    entry.getBeginEnrollment().getTime(),
                    false,
                    false);
            String friendly_date_end = DateHelper.getFriendlyDateString(
                    context,
                    entry.getEndEnrollment().getTime(),
                    false,
                    false);

            mBinding.examNameTextview.setText(entry.getName());
            mBinding.examDescriptionTextview.setText(entry.getDescription());
            mBinding.examBeginEnrollmentTextview.setText(friendly_date_begin);
            mBinding.examEndEnrollmentTextview.setText(friendly_date_end);

            mBinding.enrollButton.setOnClickListener(v -> {
                if (mExamClickCallback != null)
                    mExamClickCallback.onEnrollmentClicked(entry);
            });
        }
    }

}
