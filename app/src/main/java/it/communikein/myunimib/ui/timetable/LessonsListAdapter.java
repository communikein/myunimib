package it.communikein.myunimib.ui.timetable;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.databinding.ListItemLessonBinding;
import it.communikein.myunimib.ui.ItemActionViewHolder;
import it.communikein.myunimib.ui.timetable.LessonsListAdapter.ListItemViewHolder;

public class LessonsListAdapter extends RecyclerView.Adapter<ListItemViewHolder>  {

    private ArrayList<Lesson> mList;
    private ListItemViewHolder.OnItemActionListener onItemActionListener;


    LessonsListAdapter(ListItemViewHolder.OnItemActionListener listener) {
        this.onItemActionListener = listener;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemLessonBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_lesson,
                        parent, false);

        return new ListItemViewHolder(binding)
                .setOnItemActionListener(onItemActionListener);
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

    public Lesson getItem(int position) {
        return mList.get(position);
    }

    public void removeItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Lesson lesson, int position) {
        mList.add(position, lesson);
        notifyItemInserted(position);
    }


    static class ListItemViewHolder extends ItemActionViewHolder {

        public final ListItemLessonBinding mBinding;

        private OnItemActionListener onItemActionListener;
        public interface OnItemActionListener {
            boolean onItemShow(Lesson lesson, int position);
            boolean onItemEdit(Lesson lesson, int position);
            boolean onItemDelete(Lesson lesson, int position);
        }

        ListItemViewHolder(ListItemLessonBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
        }

        ListItemViewHolder setOnItemActionListener(OnItemActionListener listener) {
            this.onItemActionListener = listener;

            return this;
        }

        @Override
        public View getForegroundView() {
            return mBinding.foregroundView;
        }

        @Override
        public View getBackgroundView() {
            return mBinding.backgroundView;
        }

        @Override
        public boolean onItemShow() {
            onItemActionListener.onItemShow(mBinding.getLesson(), getAdapterPosition());

            return true;
        }

        @Override
        public boolean onItemEdit() {
            onItemActionListener.onItemEdit(mBinding.getLesson(), getAdapterPosition());

            return true;
        }

        @Override
        public boolean onItemDelete() {
            return onItemActionListener.onItemDelete(mBinding.getLesson(), getAdapterPosition());
        }
    }
}
