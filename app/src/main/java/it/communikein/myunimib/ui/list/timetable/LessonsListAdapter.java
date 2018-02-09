package it.communikein.myunimib.ui.list.timetable;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.databinding.ListItemLessonBinding;
import it.communikein.myunimib.ui.list.timetable.LessonsListAdapter.ListItemViewHolder;

public class LessonsListAdapter extends RecyclerView.Adapter<ListItemViewHolder> {

    private ArrayList<Lesson> mList;

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemLessonBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_lesson,
                        parent, false);

        return new ListItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        Lesson lesson = mList.get(position);

        holder.mBinding.setLesson(lesson);
        holder.mBinding.courseNameText.setText(lesson.getCourseName());
        holder.mBinding.courseClassText.setText(lesson.getBuildingClass());
        holder.mBinding.timeStartText.setText(lesson.printTimeStart());
        holder.mBinding.timeEndText.setText(lesson.printTimeEnd());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setList(final ArrayList<Lesson> newList) {
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
                    Lesson newItem = newList.get(newItemPosition);
                    Lesson oldItem = mList.get(oldItemPosition);
                    return newItem.displayEquals(oldItem);
                }
            });
            mList = newList;
            result.dispatchUpdatesTo(this);
        }
    }


    class ListItemViewHolder extends RecyclerView.ViewHolder {

        ListItemLessonBinding mBinding;

        public ListItemViewHolder(ListItemLessonBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
        }
    }
}
