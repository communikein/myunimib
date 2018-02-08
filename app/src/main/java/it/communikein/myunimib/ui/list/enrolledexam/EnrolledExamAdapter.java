package it.communikein.myunimib.ui.list.enrolledexam;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.databinding.EnrolledExamListItemBinding;
import it.communikein.myunimib.utilities.DateHelper;

import it.communikein.myunimib.ui.list.enrolledexam.EnrolledExamAdapter.ExamAdapterViewHolder;


public class EnrolledExamAdapter extends RecyclerView.Adapter<ExamAdapterViewHolder> {

    @Nullable
    private final ExamClickCallback mExamClickCallback;

    private ArrayList<EnrolledExam> mList;

    public interface ExamClickCallback {
        void onListItemClick(ExamID examID);
    }


    /**
     * Creates a BookletAdapter.
     *
     * @param examClickCallback Used to talk to the UI and app resources
     */
    EnrolledExamAdapter(@Nullable ExamClickCallback examClickCallback) {
        mExamClickCallback = examClickCallback;
    }

    @Override
    public ExamAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EnrolledExamListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.enrolled_exam_list_item,
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

    public void setList(ArrayList<EnrolledExam> newList) {
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
                    EnrolledExam newItem = newList.get(newItemPosition);
                    EnrolledExam oldItem = mList.get(oldItemPosition);

                    return newItem.getAdsceId() == oldItem.getAdsceId() &&
                            newItem.getDate().equals(oldItem.getDate()) &&
                            newItem.getDescription().equals(oldItem.getDescription()) &&
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

        final EnrolledExamListItemBinding mBinding;

        ExamAdapterViewHolder(EnrolledExamListItemBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
        }

        void bindToData() {
            EnrolledExam entry = mBinding.getExam();
            String friendly_date = DateHelper.getFriendlyDateString(
                    mBinding.horizontalMiddle.getContext(),
                    entry.getDate().getTime(),
                    false,
                    false);

            mBinding.examNameTextview.setText(entry.getName());
            mBinding.examDescriptionTextview.setText(entry.getDescription());
            mBinding.examDateTextview.setText(friendly_date);
        }
    }

}
