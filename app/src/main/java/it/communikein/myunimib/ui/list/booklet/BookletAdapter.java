package it.communikein.myunimib.ui.list.booklet;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.BookletEntry;
import it.communikein.myunimib.databinding.BookletListItemBinding;
import it.communikein.myunimib.ui.list.booklet.BookletAdapter.BookletAdapterViewHolder;


public class BookletAdapter extends RecyclerView.Adapter<BookletAdapterViewHolder> {

    @Nullable
    private final ExamClickCallback mOnClickListener;

    private ArrayList<BookletEntry> mList;

    public interface ExamClickCallback {
        void onListItemClick(int adsce_id);
    }


    /**
     * Creates a BookletAdapter.
     *
     * @param examClickCallback Used to talk to the UI and app resources
     */
    BookletAdapter(@Nullable ExamClickCallback examClickCallback) {
        mOnClickListener = examClickCallback;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent    The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public BookletAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BookletListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.booklet_list_item,
                        parent, false);
        binding.setCallback(mOnClickListener);

        return new BookletAdapterViewHolder(binding);
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
    public void onBindViewHolder(BookletAdapterViewHolder holder, int position) {
        holder.mBinding.setBookletEntry(mList.get(position));
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

    public void setList(final ArrayList<BookletEntry> newList) {
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
                    BookletEntry newItem = newList.get(newItemPosition);
                    BookletEntry oldItem = mList.get(oldItemPosition);
                    return newItem.getName().equals(oldItem.getName()) &&
                            newItem.getScore().equals(oldItem.getScore()) &&
                            newItem.getState().equals(oldItem.getState());
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
    class BookletAdapterViewHolder extends RecyclerView.ViewHolder {

        BookletListItemBinding mBinding;

        BookletAdapterViewHolder(BookletListItemBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
        }
    }

}
