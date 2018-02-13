package it.communikein.myunimib.ui.graduation.projection;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.databinding.ListItemExamProjectionBinding;
import it.communikein.myunimib.ui.ItemActionViewHolder;

public class GraduationProjectionListAdapter extends RecyclerView.Adapter<GraduationProjectionListAdapter.ListItemViewHolder> {

    private ArrayList<BookletEntry> mList;
    private ListItemViewHolder.OnItemActionListener onItemActionListener;


    GraduationProjectionListAdapter(ListItemViewHolder.OnItemActionListener listener) {
        this.onItemActionListener = listener;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemExamProjectionBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_exam_projection,
                        parent, false);

        return new ListItemViewHolder(binding)
                .setOnItemActionListener(onItemActionListener);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        BookletEntry entry = mList.get(position);

        holder.mBinding.setBookletEntry(entry);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setList(final ArrayList<BookletEntry> newList) {
        if (mList == null) {
            mList = newList;
            notifyItemRangeInserted(0, mList.size());
        } else {
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
                    BookletEntry newItem = newList.get(newItemPosition);
                    BookletEntry oldItem = mList.get(oldItemPosition);
                    return newItem.displayEquals(oldItem);
                }
            });
            mList = newList;
            result.dispatchUpdatesTo(this);
        }
    }

    public BookletEntry getItem(int position) {
        return mList.get(position);
    }

    public void removeItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(BookletEntry entry, int position) {
        mList.add(position, entry);
        notifyItemInserted(position);
    }


    static class ListItemViewHolder extends ItemActionViewHolder {

        public final ListItemExamProjectionBinding mBinding;

        private OnItemActionListener onItemActionListener;

        public interface OnItemActionListener {
            boolean onItemEdit(BookletEntry entry, int position);

            boolean onItemDelete(BookletEntry entry, int position);
        }

        ListItemViewHolder(ListItemExamProjectionBinding binding) {
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
            return true;
        }

        @Override
        public boolean onItemEdit() {
            onItemActionListener.onItemEdit(mBinding.getBookletEntry(), getAdapterPosition());

            return true;
        }

        @Override
        public boolean onItemDelete() {
            return onItemActionListener.onItemDelete(mBinding.getBookletEntry(), getAdapterPosition());
        }
    }
}
