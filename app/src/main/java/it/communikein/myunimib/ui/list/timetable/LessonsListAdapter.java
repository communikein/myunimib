package it.communikein.myunimib.ui.list.timetable;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.databinding.ListItemLessonBinding;
import it.communikein.myunimib.ui.list.timetable.LessonsListAdapter.ListItemViewHolder;

public class LessonsListAdapter extends RecyclerView.Adapter<ListItemViewHolder> {

    private ArrayList<Lesson> mList;

    private OnMenuEditClickListener mOnMenuEditClickListener;
    private OnMenuDeleteClickListener mOnMenuDeleteClickListener;

    public interface OnMenuDeleteClickListener {
        boolean onMenuDeleteClicked(Lesson lesson, int position);
    }
    public interface OnMenuEditClickListener {
        boolean onMenuEditClicked(Lesson lesson, int position);
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemLessonBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_lesson,
                        parent, false);

        return new ListItemViewHolder(binding)
                .setOnMenuDeleteClickListener(mOnMenuDeleteClickListener)
                .setOnMenuEditClickListener(mOnMenuEditClickListener);
    }

    public LessonsListAdapter setOnItemEditListener(OnMenuEditClickListener listener) {
        this.mOnMenuEditClickListener = listener;

        return this;
    }

    public LessonsListAdapter setOnItemDeleteListener(OnMenuDeleteClickListener listener) {
        this.mOnMenuDeleteClickListener = listener;

        return this;
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

    static class ListItemViewHolder extends RecyclerView.ViewHolder  implements
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        public final ListItemLessonBinding mBinding;

        private OnMenuEditClickListener onMenuEditClickListener;
        private OnMenuDeleteClickListener onMenuDeleteClickListener;

        ListItemViewHolder(ListItemLessonBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
            binding.getRoot().setOnCreateContextMenuListener(this);
        }

        ListItemViewHolder setOnMenuEditClickListener(OnMenuEditClickListener listener) {
            this.onMenuEditClickListener = listener;

            return this;
        }

        ListItemViewHolder setOnMenuDeleteClickListener(OnMenuDeleteClickListener listener) {
            this.onMenuDeleteClickListener = listener;

            return this;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem deleteAction = menu.add(Menu.NONE, R.id.action_edit, 0, R.string.action_edit);
            MenuItem editAction = menu.add(Menu.NONE, R.id.action_delete, 0, R.string.action_delete);

            deleteAction.setOnMenuItemClickListener(this);
            editAction.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    return onMenuEditClickListener
                            .onMenuEditClicked(mBinding.getLesson(), getLayoutPosition());

                    /*
                case R.id.action_delete:
                    return onMenuDeleteClickListener
                            .onMenuDeleteClicked(mBinding.getLesson(), getLayoutPosition());
                            */
            }

            return false;
        }
    }
}
